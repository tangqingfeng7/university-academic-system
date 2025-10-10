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
import java.util.stream.Collectors;
import com.university.academic.dto.converter.StatusChangeConverter;
import com.university.academic.dto.StudentStatusChangeDTO;

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
    private final StatusChangeConverter statusChangeConverter;

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
        List<StudentStatusChange> changes = statusChangeRepository.findAllByStudentId(studentId);
        // 强制初始化关联实体，避免懒加载异常
        changes.forEach(change -> {
            if (change.getStudent() != null) {
                change.getStudent().getName();
                if (change.getStudent().getMajor() != null) {
                    change.getStudent().getMajor().getName();
                }
            }
        });
        return changes;
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

        // 4. 检查学生当前状态（如果学生已经休学或退学，不能再次申请休学）
        StudentStatus currentStatus = student.getStatus();
        if (currentStatus == StudentStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, "学生当前已处于休学状态，不能重复申请休学");
        }
        if (currentStatus == StudentStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, "学生已退学，不能申请休学");
        }
        if (currentStatus == StudentStatus.GRADUATED) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, "学生已毕业，不能申请休学");
        }

        log.info("休学申请验证通过: 学生={}, 当前状态={}, 期限={}个月", 
                student.getName(), currentStatus.getDescription(), months);
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

        // 2. 检查学生当前状态是否为休学
        StudentStatus currentStatus = student.getStatus();
        if (currentStatus != StudentStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, 
                    "学生当前状态不是休学，不能申请复学。当前状态: " + currentStatus.getDescription());
        }

        // 3. 查询学生最近的已批准的休学记录，确保学生确实在休学中
        List<StudentStatusChange> history = statusChangeRepository.findAllByStudentId(student.getId());
        
        // 查找最近的已批准休学记录
        StudentStatusChange latestSuspension = history.stream()
                .filter(sc -> sc.getType() == ChangeType.SUSPENSION 
                           && sc.getStatus() == ApprovalStatus.APPROVED)
                .max((sc1, sc2) -> sc1.getCreatedAt().compareTo(sc2.getCreatedAt()))
                .orElse(null);

        if (latestSuspension == null) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, "未找到已批准的休学记录");
        }

        // 4. 验证是否已有复学申请
        boolean hasResumption = history.stream()
                .anyMatch(sc -> sc.getType() == ChangeType.RESUMPTION 
                             && sc.getStatus() == ApprovalStatus.APPROVED
                             && sc.getCreatedAt().isAfter(latestSuspension.getCreatedAt()));

        if (hasResumption) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, "已经有复学记录，不能重复申请");
        }

        // 5. 验证复学日期是否合理（不能早于休学开始日期）
        if (latestSuspension.getStartDate() != null && startDate.isBefore(latestSuspension.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "复学日期不能早于休学开始日期");
        }

        log.info("复学申请验证通过: 学生={}, 当前状态={}, 休学开始日期={}", 
                student.getName(), currentStatus.getDescription(), latestSuspension.getStartDate());
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
            throw new BusinessException(ErrorCode.TRANSFER_REQUIREMENTS_NOT_MET, "不能转入当前专业");
        }

        // 4. 检查学生当前状态（只有在读状态的学生才能转专业）
        StudentStatus currentStatus = student.getStatus();
        if (currentStatus != StudentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, 
                    "只有在读状态的学生才能申请转专业。当前状态: " + currentStatus.getDescription());
        }

        // 5. 验证学生年级限制（通常只允许大一、大二学生转专业）
        int currentYear = LocalDate.now().getYear();
        int studyYears = currentYear - student.getEnrollmentYear();
        if (studyYears > 2) {
            throw new BusinessException(ErrorCode.TRANSFER_REQUIREMENTS_NOT_MET, 
                    "转专业仅限大一、大二学生申请，您已就读" + studyYears + "年");
        }

        // 6. 检查是否有未完成的其他异动申请
        List<StudentStatusChange> history = statusChangeRepository.findAllByStudentId(student.getId());
        boolean hasOtherPendingChange = history.stream()
                .anyMatch(sc -> sc.getStatus() == ApprovalStatus.PENDING 
                             && sc.getType() != ChangeType.TRANSFER);
        
        if (hasOtherPendingChange) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_ALREADY_EXISTS, 
                    "您有其他待处理的学籍异动申请，请先完成或取消后再申请转专业");
        }

        // 7. 检查是否已有成功的转专业记录（避免频繁转专业）
        boolean hasApprovedTransfer = history.stream()
                .anyMatch(sc -> sc.getType() == ChangeType.TRANSFER 
                             && sc.getStatus() == ApprovalStatus.APPROVED);
        
        if (hasApprovedTransfer) {
            log.warn("学生 {} 已有转专业成功记录，可能不允许再次转专业", student.getName());
            // 根据学校政策决定是否允许多次转专业
            // throw new BusinessException(ErrorCode.TRANSFER_REQUIREMENTS_NOT_MET, "已有转专业记录，不能再次转专业");
        }

        // 8. 其他可扩展的验证条件（预留接口）
        // TODO: 可以进一步添加以下验证：
        // - 学生成绩要求（GPA >= 3.0）：需要查询学生的平均成绩
        // - 学分要求（已修满一定学分）：需要统计学生已获得的学分
        // - 目标专业接受转入的名额限制：需要查询目标专业的转入配额
        // - 学生无违纪记录：需要查询学生的违纪处分记录
        // - 转出专业的特殊限制：某些专业可能限制学生转出

        log.info("转专业申请验证通过: 学生={}, 当前专业={}, 目标专业={}, 入学年份={}, 就读{}年",
                student.getName(), student.getMajor().getName(), targetMajor.getName(), 
                student.getEnrollmentYear(), studyYears);
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

    @Override
    @Transactional(readOnly = true)
    public List<StudentStatusChangeDTO> getStudentHistoryDTO(Long studentId) {
        log.info("查询学生{}的异动申请DTO列表", studentId);
        
        List<StudentStatusChange> changes = statusChangeRepository.findAllByStudentId(studentId);
        
        // 在事务内完成DTO转换，确保所有关联都已加载
        return changes.stream()
                .map(change -> {
                    // 强制初始化关联实体
                    if (change.getStudent() != null) {
                        change.getStudent().getName();
                        if (change.getStudent().getMajor() != null) {
                            change.getStudent().getMajor().getName();
                        }
                    }
                    // 转换为DTO
                    return statusChangeConverter.toDTO(change, false);
                })
                .collect(Collectors.toList());
    }
}

