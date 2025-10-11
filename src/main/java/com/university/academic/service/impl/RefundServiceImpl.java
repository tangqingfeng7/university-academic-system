package com.university.academic.service.impl;

import com.university.academic.dto.*;
import com.university.academic.dto.converter.TuitionConverter;
import com.university.academic.entity.ApprovalAction;
import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.Student;
import com.university.academic.entity.User;
import com.university.academic.entity.tuition.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import com.university.academic.service.ApprovalWorkflowService;
import com.university.academic.service.RefundService;
import com.university.academic.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 退费管理服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundApplicationRepository refundApplicationRepository;
    private final RefundApprovalRepository refundApprovalRepository;
    private final TuitionPaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TuitionConverter tuitionConverter;
    private final ApprovalWorkflowService workflowService;
    private final UserNotificationService notificationService;

    @Value("${file.upload.path:/uploads/refund}")
    private String uploadPath;

    /**
     * 审批级别配置
     * 1 - 财务审核
     * 2 - 管理员审批（最终审批）
     */
    private static final int MAX_APPROVAL_LEVEL = 2;

    @Override
    @Transactional
    public RefundApplicationDTO submitRefundApplication(Long studentId, CreateRefundApplicationRequest request) {
        log.info("提交退费申请: 学生ID={}, 缴费记录ID={}", studentId, request.getPaymentId());

        // 1. 验证学生
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. 验证缴费记录
        TuitionPayment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 3. 验证缴费记录状态（只有成功的缴费才能退费）
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_CANNOT_BE_REFUNDED, "只有成功的缴费记录才能退费");
        }

        // 4. 验证缴费记录归属
        if (!payment.getBill().getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权对此缴费记录申请退费");
        }

        // 5. 验证退费金额
        if (request.getRefundAmount() <= 0) {
            throw new BusinessException(ErrorCode.REFUND_AMOUNT_INVALID);
        }
        if (request.getRefundAmount() > payment.getAmount()) {
            throw new BusinessException(ErrorCode.REFUND_AMOUNT_EXCEEDS_PAYMENT);
        }

        // 6. 检查是否已有待处理的退费申请
        List<RefundApplication> existingApplications = refundApplicationRepository
                .findByPaymentId(request.getPaymentId());
        boolean hasPendingApplication = existingApplications.stream()
                .anyMatch(app -> app.getStatus() == ApprovalStatus.PENDING);
        if (hasPendingApplication) {
            throw new BusinessException(ErrorCode.REFUND_APPLICATION_ALREADY_EXISTS);
        }

        // 7. 创建退费申请
        RefundApplication application = RefundApplication.builder()
                .student(student)
                .payment(payment)
                .refundAmount(request.getRefundAmount())
                .reason(request.getReason())
                .refundType(request.getRefundType())
                .refundMethod(request.getRefundMethod())
                .bankAccount(request.getBankAccount())
                .status(ApprovalStatus.PENDING)
                .approvalLevel(1)
                .submittedAt(LocalDateTime.now())
                .build();

        // 处理附件上传
        if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
            String attachmentUrl = uploadAttachment(request.getAttachment());
            application.setAttachmentUrl(attachmentUrl);
            log.info("附件上传成功: {}", attachmentUrl);
        }

        // 8. 分配第一级审批人（财务）
        Long firstApproverId = workflowService.assignApproverByLevel(1);
        application.setCurrentApproverId(firstApproverId);

        // 9. 保存申请
        RefundApplication savedApplication = refundApplicationRepository.save(application);

        log.info("退费申请提交成功: 申请ID={}", savedApplication.getId());

        // 10. 发送通知
        if (firstApproverId != null) {
            notificationService.sendToUser(
                    firstApproverId,
                    "退费申请待审批",
                    String.format("学生%s提交了退费申请，金额%.2f元，请及时处理。",
                            student.getName(), request.getRefundAmount()),
                    "REFUND_APPROVAL",
                    "REFUND_APPLICATION",
                    savedApplication.getId()
            );
        }

        return tuitionConverter.toRefundApplicationDTO(savedApplication);
    }

    @Override
    @Transactional
    public RefundApplication approveRefundApplication(Long applicationId, Long approverId,
                                                       ApprovalAction action, String comment) {
        log.info("审批退费申请: 申请ID={}, 审批人ID={}, 操作={}", applicationId, approverId, action);

        // 1. 查询退费申请
        RefundApplication application = refundApplicationRepository
                .findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_APPLICATION_NOT_FOUND));

        // 2. 验证申请状态
        if (application.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFUND_APPLICATION_ALREADY_PROCESSED);
        }

        // 3. 验证审批权限
        Integer currentLevel = application.getApprovalLevel();
        if (!hasRefundApprovalPermission(approverId, currentLevel)) {
            log.warn("用户无审批权限: userId={}, 申请ID={}, 当前级别={}",
                    approverId, applicationId, currentLevel);
            throw new BusinessException(ErrorCode.FORBIDDEN, "您没有权限审批该申请");
        }

        // 4. 创建审批记录
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        RefundApproval approval = RefundApproval.builder()
                .refundApplication(application)
                .approvalLevel(currentLevel)
                .approver(approver)
                .action(action)
                .comment(comment)
                .approvedAt(LocalDateTime.now())
                .build();

        refundApprovalRepository.save(approval);

        // 5. 根据审批操作更新申请状态
        switch (action) {
            case APPROVE -> handleApprove(application, currentLevel);
            case REJECT -> handleReject(application);
            case RETURN -> handleReturn(application);
        }

        // 6. 保存更新
        RefundApplication updated = refundApplicationRepository.save(application);

        // 7. 发送通知
        sendApprovalNotification(updated, action, comment, currentLevel);

        log.info("审批完成: 申请ID={}, 当前状态={}, 当前级别={}",
                applicationId, updated.getStatus(), updated.getApprovalLevel());

        return updated;
    }

    @Override
    @Transactional
    public void executeRefund(Long applicationId, Long operatorId) {
        log.info("执行退费: 申请ID={}, 操作员ID={}", applicationId, operatorId);

        // 1. 查询退费申请
        RefundApplication application = refundApplicationRepository
                .findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_APPLICATION_NOT_FOUND));

        // 2. 验证申请状态（必须已批准）
        if (application.getStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "只有已批准的申请才能执行退费");
        }

        // 3. 验证是否已退费
        if (application.getRefundedAt() != null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该申请已执行退费");
        }

        // 4. 生成退费交易号
        String refundTransactionId = generateRefundTransactionId();
        application.setRefundTransactionId(refundTransactionId);
        application.setRefundedAt(LocalDateTime.now());

        // 5. 更新缴费记录状态为已退费
        TuitionPayment payment = application.getPayment();
        payment.markAsRefunded();

        // TODO: 实际的退费操作（调用支付接口等）
        log.info("执行退费操作: 金额={}, 方式={}", application.getRefundAmount(), 
                application.getRefundMethod());

        // 6. 保存更新
        refundApplicationRepository.save(application);
        paymentRepository.save(payment);

        log.info("退费执行完成: 申请ID={}, 交易号={}", applicationId, refundTransactionId);

        // 7. 发送通知
        notificationService.sendToUser(
                application.getStudent().getId(),
                "退费成功",
                String.format("您的退费申请已处理，退费金额%.2f元已原路返回，请注意查收。",
                        application.getRefundAmount()),
                "REFUND_SUCCESS",
                "REFUND_APPLICATION",
                applicationId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundApplicationDTO> getStudentRefundApplications(Long studentId) {
        log.info("查询学生退费申请: 学生ID={}", studentId);

        List<RefundApplication> applications = refundApplicationRepository.findByStudentId(studentId);
        return tuitionConverter.toRefundApplicationDTOList(applications);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundApplicationDTO getRefundApplicationDetail(Long applicationId) {
        log.info("查询退费申请详情: 申请ID={}", applicationId);

        RefundApplication application = refundApplicationRepository
                .findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_APPLICATION_NOT_FOUND));

        // 查询审批历史
        List<RefundApproval> approvalHistory = refundApprovalRepository
                .findByRefundApplicationId(applicationId);

        RefundApplicationDTO dto = tuitionConverter.toRefundApplicationDTO(application, true);
        dto.setApprovalHistory(tuitionConverter.toRefundApprovalDTOList(approvalHistory));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundApplicationDTO> getPendingRefundApplications(Long approverId, Pageable pageable) {
        log.info("查询审批人{}的待审批退费申请", approverId);

        // 获取审批人的审批级别
        Integer approvalLevel = workflowService.getUserApprovalLevel(approverId);

        if (approvalLevel == null || approvalLevel < 1) {
            log.warn("用户{}没有审批权限", approverId);
            return Page.empty(pageable);
        }

        // 查询该审批级别的所有待审批申请
        Page<RefundApplication> pendingApplications = refundApplicationRepository
                .findPendingByApprovalLevel(approvalLevel, pageable);

        return pendingApplications.map(tuitionConverter::toRefundApplicationDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundApplicationDTO> searchRefundApplications(RefundQueryDTO query, Pageable pageable) {
        log.info("查询退费申请: query={}", query);

        // TODO: 实现复杂查询逻辑（使用Specification）
        // 这里暂时使用简单实现
        Page<RefundApplication> applications;
        if (query.getStatus() != null) {
            applications = refundApplicationRepository.findByStatus(query.getStatus(), pageable);
        } else {
            applications = refundApplicationRepository.findAll(pageable);
        }

        return applications.map(tuitionConverter::toRefundApplicationDTO);
    }

    @Override
    @Transactional
    public void cancelRefundApplication(Long applicationId, Long studentId) {
        log.info("取消退费申请: 申请ID={}, 学生ID={}", applicationId, studentId);

        // 1. 查询申请
        RefundApplication application = refundApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_APPLICATION_NOT_FOUND));

        // 2. 验证归属
        if (!application.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权取消该申请");
        }

        // 3. 验证状态（只能取消待审批的申请）
        if (application.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "只能取消待审批的申请");
        }

        // 4. 更新状态为已取消
        application.setStatus(ApprovalStatus.CANCELLED);
        application.setCurrentApproverId(null);
        refundApplicationRepository.save(application);

        log.info("退费申请已取消: 申请ID={}", applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundApplication findRefundApplicationById(Long applicationId) {
        return refundApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_APPLICATION_NOT_FOUND));
    }

    /**
     * 处理批准操作
     */
    private void handleApprove(RefundApplication application, Integer currentLevel) {
        if (currentLevel < MAX_APPROVAL_LEVEL) {
            // 还有下一级审批，流转到下一级
            Integer nextLevel = currentLevel + 1;
            application.setApprovalLevel(nextLevel);

            // 分配下一级审批人
            Long nextApproverId = workflowService.assignApproverByLevel(nextLevel);
            application.setCurrentApproverId(nextApproverId);

            log.info("审批通过，流转到下一级: 当前级别={}, 下一级别={}, 下一级审批人ID={}",
                    currentLevel, nextLevel, nextApproverId);
        } else {
            // 最终审批完成
            application.setStatus(ApprovalStatus.APPROVED);
            application.setCurrentApproverId(null);
            application.setApprovedAt(LocalDateTime.now());

            log.info("最终审批完成，申请已批准: 申请ID={}", application.getId());
        }
    }

    /**
     * 处理拒绝操作
     */
    private void handleReject(RefundApplication application) {
        application.setStatus(ApprovalStatus.REJECTED);
        application.setCurrentApproverId(null);
        log.info("审批拒绝: 申请ID={}", application.getId());
    }

    /**
     * 处理退回操作
     */
    private void handleReturn(RefundApplication application) {
        // 退回到第一级（学生可以修改后重新提交）
        application.setApprovalLevel(1);
        Long firstApproverId = workflowService.assignApproverByLevel(1);
        application.setCurrentApproverId(firstApproverId);
        log.info("审批退回: 申请ID={}", application.getId());
    }

    /**
     * 发送审批通知
     */
    private void sendApprovalNotification(RefundApplication application, 
                                          ApprovalAction action, 
                                          String comment, 
                                          Integer level) {
        String levelDesc = level == 1 ? "财务审核" : "管理员审批";
        
        // 通知学生
        String statusMsg;
        switch (action) {
            case APPROVE -> {
                if (application.getStatus() == ApprovalStatus.APPROVED) {
                    statusMsg = "您的退费申请已通过所有审批，请等待退费处理。";
                } else {
                    statusMsg = String.format("您的退费申请已通过%s，正在进入下一级审批。", levelDesc);
                }
            }
            case REJECT -> statusMsg = String.format("您的退费申请在%s阶段被拒绝。原因：%s", 
                    levelDesc, comment != null ? comment : "无");
            case RETURN -> statusMsg = String.format("您的退费申请在%s阶段被退回，请修改后重新提交。原因：%s", 
                    levelDesc, comment != null ? comment : "无");
            default -> statusMsg = "您的退费申请状态已更新。";
        }

        notificationService.sendToUser(
                application.getStudent().getId(),
                "退费申请审批通知",
                statusMsg,
                "REFUND_APPROVAL",
                "REFUND_APPLICATION",
                application.getId()
        );

        // 如果有下一级审批人，通知他
        if (application.getStatus() == ApprovalStatus.PENDING 
                && application.getCurrentApproverId() != null) {
            notificationService.sendToUser(
                    application.getCurrentApproverId(),
                    "退费申请待审批",
                    String.format("学生%s的退费申请（金额%.2f元）需要您的审批。",
                            application.getStudent().getName(),
                            application.getRefundAmount()),
                    "REFUND_APPROVAL",
                    "REFUND_APPLICATION",
                    application.getId()
            );
        }
    }

    /**
     * 生成退费交易号
     */
    private String generateRefundTransactionId() {
        return "REFUND" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 验证用户是否有退费审批权限
     * 
     * 退费审批级别：
     * 1 - 财务审核
     * 2 - 管理员审批（最终审批）
     *
     * @param userId        用户ID
     * @param approvalLevel 审批级别
     * @return 是否有权限
     */
    private boolean hasRefundApprovalPermission(Long userId, Integer approvalLevel) {
        // 使用工作流服务获取用户的审批级别
        Integer userLevel = workflowService.getUserApprovalLevel(userId);
        
        if (userLevel == -1) {
            log.debug("用户无审批权限: userId={}", userId);
            return false;
        }
        
        // 查询用户信息以进行更精确的权限判断
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getEnabled()) {
            log.debug("用户不存在或已禁用: userId={}", userId);
            return false;
        }
        
        // 退费审批权限规则：
        // 级别1（财务审核）：需要教师角色或管理员角色
        // 级别2（管理员审批）：需要管理员角色
        boolean hasPermission = switch (approvalLevel) {
            case 1 -> user.getRole() == User.UserRole.TEACHER || user.getRole() == User.UserRole.ADMIN;
            case 2 -> user.getRole() == User.UserRole.ADMIN;
            default -> false;
        };
        
        log.debug("退费审批权限检查: userId={}, userRole={}, approvalLevel={}, hasPermission={}",
                userId, user.getRole(), approvalLevel, hasPermission);
        
        return hasPermission;
    }

    /**
     * 上传附件文件
     *
     * @param file 文件
     * @return 文件URL
     */
    private String uploadAttachment(MultipartFile file) {
        try {
            // 1. 验证文件类型（只允许图片和PDF）
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
            }

            // 2. 验证文件大小（最大10MB）
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED);
            }

            // 3. 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            // 5. 保存文件
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 6. 返回相对路径
            String fileUrl = "/uploads/refund/" + filename;
            log.info("退费申请附件上传成功: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("退费申请附件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}

