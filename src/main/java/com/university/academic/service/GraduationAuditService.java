package com.university.academic.service;

import com.university.academic.dto.BatchAuditResultDTO;
import com.university.academic.dto.GraduationAuditDTO;
import com.university.academic.dto.StudentCreditSummaryDTO;
import com.university.academic.dto.converter.GraduationAuditConverter;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.GraduationAuditRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 毕业审核服务类
 * 提供毕业资格审核、学分检查、必修课检查等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationAuditService {

    private final GraduationAuditRepository graduationAuditRepository;
    private final StudentRepository studentRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final GraduationRequirementService graduationRequirementService;
    private final CreditCalculationService creditCalculationService;
    private final GraduationAuditConverter graduationAuditConverter;

    /**
     * 执行毕业审核
     * 综合检查学生的毕业资格
     *
     * @param studentId 学生ID
     * @return 毕业审核结果
     */
    @Transactional
    public GraduationAuditDTO performAudit(Long studentId) {
        log.info("开始执行毕业审核: studentId={}", studentId);

        // 1. 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. 获取学生的毕业要求
        GraduationRequirement requirement = getGraduationRequirement(student);

        // 3. 获取学生的学分汇总
        StudentCreditSummaryDTO creditSummary = creditCalculationService.calculateStudentCredits(studentId);

        // 4. 初始化审核结果
        List<String> failReasons = new ArrayList<>();
        GraduationAudit.AuditStatus auditStatus = GraduationAudit.AuditStatus.PASS;

        // 5. 检查总学分
        if (!checkTotalCredits(creditSummary, requirement, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.FAIL;
        }

        // 6. 检查必修学分
        if (!checkRequiredCredits(creditSummary, requirement, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.FAIL;
        }

        // 7. 检查选修学分
        if (!checkElectiveCredits(creditSummary, requirement, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.FAIL;
        }

        // 8. 检查实践学分
        if (!checkPracticalCredits(creditSummary, requirement, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.FAIL;
        }

        // 9. 检查必修课是否全部通过
        if (!checkRequiredCoursesPassed(studentId, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.FAIL;
        }

        // 10. 检查违纪处分情况
        if (!checkDisciplinaryStatus(student, failReasons)) {
            auditStatus = GraduationAudit.AuditStatus.DEFERRED;
        }

        // 11. 保存审核结果
        GraduationAudit audit = buildAuditRecord(student, requirement, creditSummary, 
                auditStatus, failReasons);
        audit = graduationAuditRepository.save(audit);

        log.info("毕业审核完成: studentId={}, status={}, failReasons={}", 
                studentId, auditStatus, failReasons);

        return graduationAuditConverter.toDTO(audit);
    }

    /**
     * 获取学生的毕业要求
     *
     * @param student 学生实体
     * @return 毕业要求
     */
    private GraduationRequirement getGraduationRequirement(Student student) {
        try {
            return graduationRequirementService.findEntityById(
                    graduationRequirementService.findByMajorAndYear(
                            student.getMajor().getId(),
                            student.getEnrollmentYear()
                    ).getId()
            );
        } catch (BusinessException e) {
            log.error("未找到毕业要求: majorId={}, enrollmentYear={}", 
                    student.getMajor().getId(), student.getEnrollmentYear());
            throw new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND);
        }
    }

    /**
     * 检查总学分是否达标
     *
     * @param creditSummary 学分汇总
     * @param requirement   毕业要求
     * @param failReasons   不通过原因列表
     * @return 是否达标
     */
    private boolean checkTotalCredits(StudentCreditSummaryDTO creditSummary,
                                      GraduationRequirement requirement,
                                      List<String> failReasons) {
        double totalCredits = creditSummary.getTotalCredits();
        double requiredTotalCredits = requirement.getTotalCreditsRequired();

        if (totalCredits < requiredTotalCredits) {
            String reason = String.format("总学分不足：已修%.1f学分，要求%.1f学分，还差%.1f学分",
                    totalCredits, requiredTotalCredits, requiredTotalCredits - totalCredits);
            failReasons.add(reason);
            log.warn("总学分检查未通过: {}", reason);
            return false;
        }

        return true;
    }

    /**
     * 检查必修学分是否达标
     *
     * @param creditSummary 学分汇总
     * @param requirement   毕业要求
     * @param failReasons   不通过原因列表
     * @return 是否达标
     */
    private boolean checkRequiredCredits(StudentCreditSummaryDTO creditSummary,
                                         GraduationRequirement requirement,
                                         List<String> failReasons) {
        double requiredCredits = creditSummary.getRequiredCredits();
        double requiredRequiredCredits = requirement.getRequiredCreditsRequired();

        if (requiredCredits < requiredRequiredCredits) {
            String reason = String.format("必修学分不足：已修%.1f学分，要求%.1f学分，还差%.1f学分",
                    requiredCredits, requiredRequiredCredits, requiredRequiredCredits - requiredCredits);
            failReasons.add(reason);
            log.warn("必修学分检查未通过: {}", reason);
            return false;
        }

        return true;
    }

    /**
     * 检查选修学分是否达标
     *
     * @param creditSummary 学分汇总
     * @param requirement   毕业要求
     * @param failReasons   不通过原因列表
     * @return 是否达标
     */
    private boolean checkElectiveCredits(StudentCreditSummaryDTO creditSummary,
                                         GraduationRequirement requirement,
                                         List<String> failReasons) {
        double electiveCredits = creditSummary.getElectiveCredits();
        double requiredElectiveCredits = requirement.getElectiveCreditsRequired();

        if (electiveCredits < requiredElectiveCredits) {
            String reason = String.format("选修学分不足：已修%.1f学分，要求%.1f学分，还差%.1f学分",
                    electiveCredits, requiredElectiveCredits, requiredElectiveCredits - electiveCredits);
            failReasons.add(reason);
            log.warn("选修学分检查未通过: {}", reason);
            return false;
        }

        return true;
    }

    /**
     * 检查实践学分是否达标
     *
     * @param creditSummary 学分汇总
     * @param requirement   毕业要求
     * @param failReasons   不通过原因列表
     * @return 是否达标
     */
    private boolean checkPracticalCredits(StudentCreditSummaryDTO creditSummary,
                                          GraduationRequirement requirement,
                                          List<String> failReasons) {
        double practicalCredits = creditSummary.getPracticalCredits();
        double requiredPracticalCredits = requirement.getPracticalCreditsRequired();

        if (practicalCredits < requiredPracticalCredits) {
            String reason = String.format("实践学分不足：已修%.1f学分，要求%.1f学分，还差%.1f学分",
                    practicalCredits, requiredPracticalCredits, requiredPracticalCredits - practicalCredits);
            failReasons.add(reason);
            log.warn("实践学分检查未通过: {}", reason);
            return false;
        }

        return true;
    }

    /**
     * 检查必修课是否全部通过
     *
     * @param studentId   学生ID
     * @param failReasons 不通过原因列表
     * @return 是否全部通过
     */
    private boolean checkRequiredCoursesPassed(Long studentId, List<String> failReasons) {
        // 查询所有已完成的必修课选课记录
        List<CourseSelection> completedSelections = courseSelectionRepository
                .findByStudentIdAndStatus(studentId, CourseSelection.SelectionStatus.COMPLETED);

        // 筛选出必修课
        List<CourseSelection> requiredCourseSelections = completedSelections.stream()
                .filter(selection -> selection.getOffering().getCourse().getType() == Course.CourseType.REQUIRED)
                .collect(Collectors.toList());

        // 检查是否有未通过的必修课
        List<String> failedCourses = new ArrayList<>();
        for (CourseSelection selection : requiredCourseSelections) {
            Grade grade = selection.getGrade();
            if (grade != null && grade.getStatus() == Grade.GradeStatus.PUBLISHED) {
                if (!grade.isPassed()) {
                    String courseName = selection.getOffering().getCourse().getName();
                    failedCourses.add(courseName);
                }
            }
        }

        if (!failedCourses.isEmpty()) {
            String reason = "以下必修课未通过：" + String.join("、", failedCourses);
            failReasons.add(reason);
            log.warn("必修课检查未通过: {}", reason);
            return false;
        }

        return true;
    }

    /**
     * 检查违纪处分情况
     * 如果有未解除的违纪处分，则暂缓毕业
     *
     * @param student     学生实体
     * @param failReasons 不通过原因列表
     * @return 是否符合毕业条件
     */
    private boolean checkDisciplinaryStatus(Student student, List<String> failReasons) {
        // TODO: 实现违纪处分检查逻辑
        // 这里需要集成违纪处分管理模块
        // 暂时默认通过

        // 示例逻辑（待实现）：
        // if (student.hasPendingDisciplinaryAction()) {
        //     String reason = "存在未解除的违纪处分，暂缓毕业";
        //     failReasons.add(reason);
        //     log.warn("违纪处分检查未通过: studentId={}", student.getId());
        //     return false;
        // }

        return true;
    }

    /**
     * 构建审核记录
     *
     * @param student       学生实体
     * @param requirement   毕业要求
     * @param creditSummary 学分汇总
     * @param status        审核状态
     * @param failReasons   不通过原因
     * @return 审核记录
     */
    private GraduationAudit buildAuditRecord(Student student,
                                             GraduationRequirement requirement,
                                             StudentCreditSummaryDTO creditSummary,
                                             GraduationAudit.AuditStatus status,
                                             List<String> failReasons) {
        GraduationAudit audit = new GraduationAudit();
        audit.setStudent(student);
        audit.setAuditYear(Year.now().getValue());
        audit.setTotalCredits(creditSummary.getTotalCredits());
        audit.setRequiredCredits(creditSummary.getRequiredCredits());
        audit.setElectiveCredits(creditSummary.getElectiveCredits());
        audit.setPracticalCredits(creditSummary.getPracticalCredits());
        audit.setStatus(status);

        // 设置不通过原因
        if (!failReasons.isEmpty()) {
            audit.setFailReason(String.join("；", failReasons));
        }

        // 设置审核时间和审核人
        audit.setAuditedAt(LocalDateTime.now());
        
        // 从SecurityContext获取当前登录用户ID作为审核人
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            audit.setAuditedBy(currentUserId);
            log.debug("设置审核人ID: {}", currentUserId);
        } else {
            log.warn("无法获取当前用户ID，审核记录将不包含审核人信息");
        }

        return audit;
    }

    /**
     * 查询学生的审核结果
     *
     * @param studentId 学生ID
     * @return 审核结果
     */
    @Transactional(readOnly = true)
    public GraduationAuditDTO getAuditResult(Long studentId) {
        log.debug("查询学生审核结果: studentId={}", studentId);

        // 查询最新的审核记录
        GraduationAudit audit = graduationAuditRepository
                .findTopByStudentIdOrderByAuditedAtDesc(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_AUDIT_NOT_FOUND));

        return graduationAuditConverter.toDTO(audit);
    }

    /**
     * 查询学生指定年份的审核结果
     *
     * @param studentId 学生ID
     * @param year      审核年份
     * @return 审核结果
     */
    @Transactional(readOnly = true)
    public GraduationAuditDTO getAuditResultByYear(Long studentId, Integer year) {
        log.debug("查询学生审核结果: studentId={}, year={}", studentId, year);

        GraduationAudit audit = graduationAuditRepository
                .findByStudentIdAndAuditYearWithDetails(studentId, year)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_AUDIT_NOT_FOUND));

        return graduationAuditConverter.toDTO(audit);
    }

    /**
     * 查询所有审核记录（分页）
     *
     * @param year   审核年份（可选）
     * @param status 审核状态（可选）
     * @return 审核记录列表
     */
    @Transactional(readOnly = true)
    public List<GraduationAuditDTO> getAuditRecords(Integer year, GraduationAudit.AuditStatus status) {
        log.debug("查询审核记录: year={}, status={}", year, status);

        List<GraduationAudit> audits;

        if (year != null && status != null) {
            audits = graduationAuditRepository.findByStatusAndAuditYearWithDetails(status, year);
        } else if (year != null) {
            audits = graduationAuditRepository.findByAuditYearWithDetails(year);
        } else if (status != null) {
            audits = graduationAuditRepository.findByStatusWithDetails(status);
        } else {
            audits = graduationAuditRepository.findAllWithDetails();
        }

        return graduationAuditConverter.toDTOList(audits);
    }

    /**
     * 重新审核（更新审核结果）
     *
     * @param studentId 学生ID
     * @return 审核结果
     */
    @Transactional
    public GraduationAuditDTO reAudit(Long studentId) {
        log.info("重新执行毕业审核: studentId={}", studentId);

        // 执行新的审核
        return performAudit(studentId);
    }

    /**
     * 批量毕业审核
     * 使用事务处理,保证数据一致性
     *
     * @param studentIds 学生ID列表
     * @return 审核结果汇总
     */
    @Transactional
    public BatchAuditResultDTO batchPerformAudit(List<Long> studentIds) {
        log.info("开始批量毕业审核: count={}", studentIds.size());

        int successCount = 0;
        int failCount = 0;
        int passCount = 0;
        int notPassCount = 0;
        int deferredCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long studentId : studentIds) {
            try {
                GraduationAuditDTO result = performAudit(studentId);
                successCount++;

                // 统计审核状态
                switch (result.getStatus()) {
                    case PASS:
                        passCount++;
                        break;
                    case FAIL:
                        notPassCount++;
                        break;
                    case DEFERRED:
                        deferredCount++;
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                failCount++;
                String error = String.format("学生ID %d 审核失败: %s", studentId, e.getMessage());
                errors.add(error);
                log.error("批量审核失败: studentId={}, error={}", studentId, e.getMessage(), e);
            }
        }

        log.info("批量毕业审核完成: 成功={}, 失败={}, 通过={}, 不通过={}, 暂缓={}", 
                successCount, failCount, passCount, notPassCount, deferredCount);

        return BatchAuditResultDTO.builder()
                .totalCount(studentIds.size())
                .successCount(successCount)
                .failCount(failCount)
                .passCount(passCount)
                .notPassCount(notPassCount)
                .deferredCount(deferredCount)
                .errors(errors)
                .build();
    }
}

