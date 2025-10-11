package com.university.academic.controller;

import com.university.academic.dto.CourseSelectionDTO;
import com.university.academic.dto.GraduationAuditDTO;
import com.university.academic.dto.GraduationProgressDTO;
import com.university.academic.dto.GraduationRequirementDTO;
import com.university.academic.dto.StudentCreditSummaryDTO;
import com.university.academic.entity.Student;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.CreditCalculationService;
import com.university.academic.service.GraduationAuditService;
import com.university.academic.service.GraduationRequirementService;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端毕业审核Controller
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/graduation")
@RequiredArgsConstructor
@Tag(name = "学生端毕业审核管理", description = "学生查询毕业审核结果和学分信息")
public class StudentGraduationController {

    private final GraduationAuditService graduationAuditService;
    private final CreditCalculationService creditCalculationService;
    private final GraduationRequirementService graduationRequirementService;
    private final StudentRepository studentRepository;

    /**
     * 获取当前登录学生的student_id
     * 
     * @return student_id
     */
    private Long getCurrentStudentId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        return student.getId();
    }

    /**
     * 查询我的审核结果
     */
    @GetMapping("/audit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的审核结果", description = "查询当前学生的最新毕业审核结果")
    public Result<GraduationAuditDTO> getMyAuditResult() {
        Long studentId = getCurrentStudentId();
        log.info("查询学生审核结果: studentId={}", studentId);

        try {
            GraduationAuditDTO result = graduationAuditService.getAuditResult(studentId);
            return Result.success(result);
        } catch (Exception e) {
            // 如果没有审核记录,返回null而不是错误,让前端显示友好提示
            log.debug("学生尚未进行毕业审核: studentId={}", studentId);
            return Result.success(null);
        }
    }

    /**
     * 查询我的学分汇总
     */
    @GetMapping("/credits")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的学分汇总", description = "查询当前学生的学分统计信息")
    public Result<StudentCreditSummaryDTO> getMyCreditSummary() {
        Long studentId = getCurrentStudentId();
        log.info("查询学生学分汇总: studentId={}", studentId);

        StudentCreditSummaryDTO summary = creditCalculationService.getStudentCreditSummary(studentId);
        return Result.success(summary);
    }

    /**
     * 查询毕业进度
     * 包含学分完成情况、审核状态和毕业要求
     */
    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询毕业进度", description = "查询当前学生的毕业进度信息")
    public Result<GraduationProgressDTO> getGraduationProgress() {
        Long studentId = getCurrentStudentId();
        log.info("查询毕业进度: studentId={}", studentId);

        // 获取学分汇总
        StudentCreditSummaryDTO credits = creditCalculationService.getStudentCreditSummary(studentId);

        // 获取毕业要求
        GraduationRequirementDTO requirement = null;
        if (credits.getMajorId() != null && credits.getEnrollmentYear() != null) {
            try {
                requirement = graduationRequirementService.findByMajorAndYear(
                        credits.getMajorId(), credits.getEnrollmentYear());
            } catch (Exception e) {
                log.warn("未找到毕业要求: majorId={}, enrollmentYear={}", 
                        credits.getMajorId(), credits.getEnrollmentYear());
            }
        }

        // 获取审核结果
        GraduationAuditDTO audit = null;
        try {
            audit = graduationAuditService.getAuditResult(studentId);
        } catch (Exception e) {
            log.debug("学生尚未进行毕业审核: studentId={}", studentId);
        }

        // 构建进度DTO
        GraduationProgressDTO progress = GraduationProgressDTO.builder()
                .credits(credits)
                .requirement(requirement)
                .audit(audit)
                .build();

        return Result.success(progress);
    }

    /**
     * 查询学分明细（已完成课程列表）
     */
    @GetMapping("/credits/details")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询学分明细", description = "查询当前学生所有已完成课程的学分明细")
    public Result<List<CourseSelectionDTO>> getCreditDetails() {
        Long studentId = getCurrentStudentId();
        log.info("查询学生学分明细: studentId={}", studentId);

        List<CourseSelectionDTO> details = creditCalculationService.getCompletedCourseDetails(studentId);
        return Result.success(details);
    }
}

