package com.university.academic.controller;

import com.university.academic.dto.*;
import com.university.academic.entity.Student;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.ScholarshipApplicationService;
import com.university.academic.service.ScholarshipAwardService;
import com.university.academic.service.ScholarshipService;
import com.university.academic.service.ScholarshipApprovalService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端奖学金Controller
 * 提供学生查询奖学金、申请、查看获奖记录等功能
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentScholarshipController {
    
    private final ScholarshipService scholarshipService;
    private final ScholarshipApplicationService applicationService;
    private final ScholarshipAwardService awardService;
    private final StudentRepository studentRepository;
    private final ScholarshipApprovalService approvalService;
    
    /**
     * 获取当前登录学生的studentId
     */
    private Long getCurrentStudentId() {
        Long userId = SecurityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return student.getId();
    }
    
    // ==================== 奖学金查询 ====================
    
    /**
     * 查询可申请的奖学金列表
     */
    @GetMapping("/scholarships/available")
    public Result<List<ScholarshipDTO>> getAvailableScholarships() {
        log.info("查询可申请的奖学金列表");
        List<ScholarshipDTO> scholarships = scholarshipService.getActiveScholarships();
        return Result.success(scholarships);
    }
    
    /**
     * 查询奖学金详情
     */
    @GetMapping("/scholarships/{id}")
    public Result<ScholarshipDTO> getScholarship(@PathVariable Long id) {
        log.info("查询奖学金详情: id={}", id);
        ScholarshipDTO scholarship = scholarshipService.getScholarshipById(id);
        return Result.success(scholarship);
    }
    
    /**
     * 验证申请资格
     */
    @GetMapping("/scholarships/{scholarshipId}/eligibility")
    public Result<Boolean> checkEligibility(@PathVariable Long scholarshipId) {
        Long studentId = getCurrentStudentId();
        log.info("验证申请资格: studentId={}, scholarshipId={}", studentId, scholarshipId);
        boolean eligible = applicationService.validateEligibility(studentId, scholarshipId);
        return Result.success(eligible);
    }
    
    // ==================== 奖学金申请 ====================
    
    /**
     * 提交申请
     */
    @PostMapping("/scholarship-applications")
    public Result<ScholarshipApplicationDTO> submitApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        Long studentId = getCurrentStudentId();
        log.info("学生提交奖学金申请: studentId={}, scholarshipId={}", 
                studentId, request.getScholarshipId());
        ScholarshipApplicationDTO application = applicationService.submitApplication(studentId, request);
        return Result.success(application);
    }
    
    /**
     * 查询我的申请列表
     */
    @GetMapping("/scholarship-applications")
    public Result<List<ScholarshipApplicationDTO>> getMyApplications() {
        Long studentId = getCurrentStudentId();
        log.info("查询我的申请: studentId={}", studentId);
        List<ScholarshipApplicationDTO> applications = applicationService.getStudentApplications(studentId);
        return Result.success(applications);
    }
    
    /**
     * 查询申请详情
     */
    @GetMapping("/scholarship-applications/{id}")
    public Result<ScholarshipApplicationDTO> getApplication(@PathVariable Long id) {
        Long studentId = getCurrentStudentId();
        log.info("查询申请详情: id={}, studentId={}", id, studentId);
        
        ScholarshipApplicationDTO application = applicationService.getApplicationById(id);
        
        // 验证是否为本人的申请
        if (!application.getStudent().getId().equals(studentId)) {
            return Result.error(403, "无权查看该申请");
        }
        
        return Result.success(application);
    }
    
    /**
     * 撤销申请
     */
    @DeleteMapping("/scholarship-applications/{id}")
    public Result<Void> cancelApplication(@PathVariable Long id) {
        Long studentId = getCurrentStudentId();
        log.info("撤销申请: id={}, studentId={}", id, studentId);
        applicationService.cancelApplication(id, studentId);
        return Result.success();
    }
    
    /**
     * 查询申请的审批流程和审批历史
     */
    @GetMapping("/scholarship-applications/{id}/approval-flow")
    public Result<List<ScholarshipApprovalDTO>> getApprovalFlow(@PathVariable Long id) {
        Long studentId = getCurrentStudentId();
        log.info("查询审批流程: applicationId={}, studentId={}", id, studentId);
        
        // 验证申请是否属于当前学生
        ScholarshipApplicationDTO application = applicationService.getApplicationById(id);
        if (!application.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 获取审批历史
        List<ScholarshipApprovalDTO> history = approvalService.getApprovalHistory(id);
        return Result.success(history);
    }
    
    // ==================== 获奖记录 ====================
    
    /**
     * 查询我的获奖记录
     */
    @GetMapping("/scholarship-awards")
    public Result<List<ScholarshipAwardDTO>> getMyAwards() {
        Long studentId = getCurrentStudentId();
        log.info("查询我的获奖记录: studentId={}", studentId);
        List<ScholarshipAwardDTO> awards = awardService.getStudentAwards(studentId);
        return Result.success(awards);
    }
}

