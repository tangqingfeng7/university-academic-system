package com.university.academic.controller;

import com.university.academic.dto.*;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.ScholarshipApplicationService;
import com.university.academic.service.ScholarshipApprovalService;
import com.university.academic.service.ScholarshipService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批人端奖学金Controller
 * 提供审批奖学金申请的功能
 */
@RestController
@RequestMapping("/api/approver")
@RequiredArgsConstructor
@Slf4j
public class ApproverScholarshipController {
    
    private final ScholarshipApplicationService applicationService;
    private final ScholarshipApprovalService approvalService;
    private final ScholarshipService scholarshipService;
    
    // ==================== 奖学金查询 ====================
    
    /**
     * 查询奖学金列表（用于筛选）
     */
    @GetMapping("/scholarships")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Page<ScholarshipDTO>> getScholarships(
            @PageableDefault(size = 100, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("查询奖学金列表");
        Page<ScholarshipDTO> scholarships = scholarshipService.getScholarships(pageable);
        return Result.success(scholarships);
    }
    
    // ==================== 待审批申请查询 ====================
    
    /**
     * 查询待审批的申请列表
     */
    @GetMapping("/scholarship-applications/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Page<ScholarshipApplicationDTO>> getPendingApplications(
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("查询待审批申请: userId={}", userId);
        
        // 构建查询条件：待审核或辅导员已批准的申请
        ApplicationQueryDTO query = new ApplicationQueryDTO();
        // 根据用户角色设置不同的查询条件
        // 这里简化处理，实际应该根据角色和权限查询
        
        Page<ScholarshipApplicationDTO> applications = applicationService.getApplications(query, pageable);
        return Result.success(applications);
    }
    
    /**
     * 查询申请详情
     */
    @GetMapping("/scholarship-applications/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<ScholarshipApplicationDTO> getApplication(@PathVariable Long id) {
        log.info("查询申请详情: id={}", id);
        ScholarshipApplicationDTO application = applicationService.getApplicationById(id);
        return Result.success(application);
    }
    
    /**
     * 检查是否可以审批
     */
    @GetMapping("/scholarship-applications/{id}/can-approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Boolean> canApprove(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("检查是否可以审批: applicationId={}, userId={}", id, userId);
        boolean canApprove = approvalService.canApprove(id, userId);
        return Result.success(canApprove);
    }
    
    /**
     * 查询审批流程
     */
    @GetMapping("/scholarship-applications/{id}/approval-flow")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<ApprovalFlowDTO> getApprovalFlow(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("查询审批流程: applicationId={}, userId={}", id, userId);
        ApprovalFlowDTO flow = approvalService.getApprovalFlow(id, userId);
        return Result.success(flow);
    }
    
    /**
     * 查询审批历史
     */
    @GetMapping("/scholarship-applications/{id}/approval-history")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<ScholarshipApprovalDTO>> getApprovalHistory(@PathVariable Long id) {
        log.info("查询审批历史: applicationId={}", id);
        List<ScholarshipApprovalDTO> history = approvalService.getApprovalHistory(id);
        return Result.success(history);
    }
    
    // ==================== 审批操作 ====================
    
    /**
     * 辅导员审核
     */
    @PutMapping("/scholarship-applications/{id}/counselor-approve")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<ScholarshipApplicationDTO> counselorApprove(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        Long approverId = SecurityUtils.getCurrentUserId();
        log.info("辅导员审核奖学金申请: id={}, approverId={}, action={}", 
                id, approverId, request.getAction());
        ScholarshipApplicationDTO application = approvalService.counselorApprove(id, approverId, request);
        return Result.success(application);
    }
    
    /**
     * 院系审批
     */
    @PutMapping("/scholarship-applications/{id}/department-approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    public Result<ScholarshipApplicationDTO> departmentApprove(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        Long approverId = SecurityUtils.getCurrentUserId();
        log.info("院系审批奖学金申请: id={}, approverId={}, action={}", 
                id, approverId, request.getAction());
        ScholarshipApplicationDTO application = approvalService.departmentApprove(id, approverId, request);
        return Result.success(application);
    }
}

