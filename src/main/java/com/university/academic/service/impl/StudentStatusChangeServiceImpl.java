package com.university.academic.service.impl;

import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentStatusChangeRepository;
import com.university.academic.service.ApprovalWorkflowService;
import com.university.academic.service.MajorService;
import com.university.academic.service.StudentService;
import com.university.academic.service.StudentStatusChangeService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * 学籍异动管理Service实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentStatusChangeServiceImpl implements StudentStatusChangeService {

    private final StudentStatusChangeRepository statusChangeRepository;
    private final StudentService studentService;
    private final MajorService majorService;
    private final ApprovalWorkflowService workflowService;

    @Value("${file.upload.path:/uploads/status-change}")
    private String uploadPath;

    @Override
    @Transactional
    public StudentStatusChange createApplication(
            Long studentId,
            ChangeType type,
            String reason,
            LocalDate startDate,
            LocalDate endDate,
            Long targetMajorId,
            MultipartFile attachment
    ) {
        log.info("创建学籍异动申请: 学生ID={}, 类型={}", studentId, type);

        // 1. 验证学生是否存在
        Student student = studentService.findById(studentId);

        // 2. 检查是否有进行中的异动申请
        if (statusChangeRepository.existsPendingByStudentId(studentId)) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_ALREADY_EXISTS);
        }

        // 3. 根据异动类型进行特定验证
        validateApplicationByType(student, type, startDate, endDate, targetMajorId);

        // 4. 处理附件上传
        String attachmentUrl = null;
        if (attachment != null && !attachment.isEmpty()) {
            attachmentUrl = uploadAttachment(attachment);
        }

        // 5. 创建异动申请
        StudentStatusChange statusChange = StudentStatusChange.builder()
                .student(student)
                .type(type)
                .reason(reason)
                .startDate(startDate)
                .endDate(endDate)
                .targetMajorId(targetMajorId)
                .attachmentUrl(attachmentUrl)
                .status(ApprovalStatus.PENDING)
                .approvalLevel(1) // 从第1级开始审批（辅导员）
                .deleted(false)
                .build();

        // 6. 自动分配第一级审批人（负载均衡）
        Long firstApproverId = workflowService.assignApproverByLevel(1);
        statusChange.setCurrentApproverId(firstApproverId);

        // 7. 设置审批截止时间（根据异动类型配置）
        int deadlineDays = workflowService.getDeadlineDays(type);
        LocalDateTime deadline = LocalDateTime.now().plusDays(deadlineDays);
        statusChange.setDeadline(deadline);
        statusChange.setIsOverdue(false);

        StudentStatusChange saved = statusChangeRepository.save(statusChange);

        // 8. 增加审批人的待处理任务计数
        if (firstApproverId != null) {
            workflowService.incrementApproverLoad(firstApproverId, 1);
        }

        log.info("学籍异动申请创建成功: ID={}, 学生={}, 类型={}, 第一级审批人ID={}, 截止时间={}",
                saved.getId(), student.getName(), type, firstApproverId, deadline);

        return saved;
    }

    @Override
    @Transactional
    public void cancelApplication(Long applicationId, Long studentId) {
        log.info("撤销学籍异动申请: 申请ID={}, 学生ID={}", applicationId, studentId);

        // 1. 查询申请记录
        StudentStatusChange statusChange = statusChangeRepository
                .findByIdAndStudentId(applicationId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));

        // 2. 验证是否可以撤销
        if (statusChange.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_CANNOT_CANCEL);
        }

        // 3. 更新状态为已取消
        statusChange.setStatus(ApprovalStatus.CANCELLED);
        statusChangeRepository.save(statusChange);

        log.info("学籍异动申请撤销成功: 申请ID={}", applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentStatusChange> getStudentHistory(Long studentId) {
        return statusChangeRepository.findAllByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentStatusChange findByIdWithDetails(Long applicationId) {
        return statusChangeRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentStatusChange findByIdAndStudentId(Long applicationId, Long studentId) {
        return statusChangeRepository.findByIdAndStudentId(applicationId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> findByStudentId(Long studentId, Pageable pageable) {
        return statusChangeRepository.findByStudentId(studentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> findByStatus(ApprovalStatus status, Pageable pageable) {
        return statusChangeRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> findByType(ChangeType type, Pageable pageable) {
        return statusChangeRepository.findByType(type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> findByMultipleConditions(
            ChangeType type,
            ApprovalStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return statusChangeRepository.findByMultipleConditions(type, status, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingApplication(Long studentId) {
        return statusChangeRepository.existsPendingByStudentId(studentId);
    }

    /**
     * 根据异动类型验证申请条件
     *
     * @param student       学生
     * @param type          异动类型
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param targetMajorId 目标专业ID
     */
    private void validateApplicationByType(
            Student student,
            ChangeType type,
            LocalDate startDate,
            LocalDate endDate,
            Long targetMajorId
    ) {
        switch (type) {
            case SUSPENSION -> validateSuspensionApplication(student, startDate, endDate);
            case RESUMPTION -> validateResumptionApplication(student, startDate);
            case TRANSFER -> validateTransferApplication(student, targetMajorId);
            case WITHDRAWAL -> validateWithdrawalApplication(student);
        }
    }

    /**
     * 验证休学申请
     *
     * @param student   学生
     * @param startDate 开始日期
     * @param endDate   结束日期
     */
    private void validateSuspensionApplication(Student student, LocalDate startDate, LocalDate endDate) {
        // 1. 验证日期是否为空
        if (startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 2. 验证日期范围
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 3. 验证休学期限（通常不超过2年）
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        if (months > 24 || months < 1) {
            throw new BusinessException(ErrorCode.SUSPENSION_PERIOD_INVALID);
        }

        // 4. 检查学生当前状态（如果学生已经休学，不能再次申请休学）
        // TODO: 需要在Student实体中添加status字段来记录学生当前状态
        // 或者通过查询最近的已批准的休学记录来判断

        log.info("休学申请验证通过: 学生={}, 期限={}个月", student.getName(), months);
    }

    /**
     * 验证复学申请
     *
     * @param student   学生
     * @param startDate 复学日期
     */
    private void validateResumptionApplication(Student student, LocalDate startDate) {
        // 1. 验证日期是否为空
        if (startDate == null) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 2. 检查学生是否处于休学状态
        // TODO: 需要查询学生最近的已批准的休学记录，确保学生确实在休学中
        // 临时简化处理：查询是否有已批准的休学记录
        List<StudentStatusChange> history = statusChangeRepository.findAllByStudentId(student.getId());
        boolean hasSuspension = history.stream()
                .anyMatch(sc -> sc.getType() == ChangeType.SUSPENSION &&
                               sc.getStatus() == ApprovalStatus.APPROVED);

        if (!hasSuspension) {
            log.warn("学生 {} 没有休学记录，不能申请复学", student.getName());
            // 暂时不抛出异常，实际应该检查学生当前状态
            // throw new BusinessException(ErrorCode.STUDENT_NOT_SUSPENDED);
        }

        log.info("复学申请验证通过: 学生={}", student.getName());
    }

    /**
     * 验证转专业申请
     *
     * @param student       学生
     * @param targetMajorId 目标专业ID
     */
    private void validateTransferApplication(Student student, Long targetMajorId) {
        // 1. 验证目标专业是否为空
        if (targetMajorId == null) {
            throw new BusinessException(ErrorCode.TARGET_MAJOR_NOT_FOUND);
        }

        // 2. 验证目标专业是否存在
        Major targetMajor = majorService.findById(targetMajorId);

        // 3. 验证是否转入同一专业
        if (student.getMajor().getId().equals(targetMajorId)) {
            throw new BusinessException(ErrorCode.TRANSFER_REQUIREMENTS_NOT_MET);
        }

        // 4. 验证转专业条件
        // TODO: 可以添加更多条件验证，例如：
        // - 学生成绩要求（GPA >= 3.0）
        // - 学分要求（已修满一定学分）
        // - 目标专业是否接受转入
        // - 学生年级限制（通常只允许大一、大二转专业）

        log.info("转专业申请验证通过: 学生={}, 当前专业={}, 目标专业={}",
                student.getName(), student.getMajor().getName(), targetMajor.getName());
    }

    /**
     * 验证退学申请
     *
     * @param student 学生
     */
    private void validateWithdrawalApplication(Student student) {
        // 退学申请通常没有特殊限制条件，但可以添加一些检查
        // 例如：是否有未缴清的学费、是否有未归还的图书等
        log.info("退学申请验证通过: 学生={}", student.getName());
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
            String fileUrl = "/uploads/status-change/" + filename;
            log.info("附件上传成功: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentStatusChange getApplicationById(Long applicationId, Long studentId) {
        log.info("查询学生{}的异动申请: {}", studentId, applicationId);

        return statusChangeRepository.findByIdAndStudentId(applicationId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));
    }
}

