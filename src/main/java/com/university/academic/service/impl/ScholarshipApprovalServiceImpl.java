package com.university.academic.service.impl;

import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ScholarshipApplicationRepository;
import com.university.academic.repository.ScholarshipApprovalRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.ScholarshipApprovalService;
import com.university.academic.util.DtoConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 奖学金审批服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipApprovalServiceImpl implements ScholarshipApprovalService {
    
    private final ScholarshipApplicationRepository applicationRepository;
    private final ScholarshipApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    
    // 审批级别常量
    private static final int COUNSELOR_LEVEL = 1;
    private static final int DEPARTMENT_LEVEL = 2;
    private static final int MAX_APPROVAL_LEVEL = 2;
    
    /**
     * 辅导员审核
     */
    @Override
    @Transactional
    public ScholarshipApplicationDTO counselorApprove(Long applicationId, Long approverId, ApprovalRequest request) {
        log.info("辅导员审核奖学金申请: applicationId={}, approverId={}, action={}", 
                applicationId, approverId, request.getAction());
        return approve(applicationId, approverId, COUNSELOR_LEVEL, request);
    }
    
    /**
     * 院系审批
     */
    @Override
    @Transactional
    public ScholarshipApplicationDTO departmentApprove(Long applicationId, Long approverId, ApprovalRequest request) {
        log.info("院系审批奖学金申请: applicationId={}, approverId={}, action={}", 
                applicationId, approverId, request.getAction());
        return approve(applicationId, approverId, DEPARTMENT_LEVEL, request);
    }
    
    /**
     * 通用审批方法
     */
    @Override
    @Transactional
    public ScholarshipApplicationDTO approve(
            Long applicationId, Long approverId, Integer approvalLevel, ApprovalRequest request) {
        
        // 1. 查询申请
        ScholarshipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_FOUND));
        
        // 2. 查询审批人
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 3. 检查申请状态
        validateApplicationStatus(application, approvalLevel);
        
        // 4. 检查是否已审批过
        if (approvalRepository.existsByApplicationIdAndApprovalLevel(applicationId, approvalLevel)) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_ALREADY_APPROVED);
        }
        
        // 5. 创建审批记录
        ScholarshipApproval approval = new ScholarshipApproval();
        approval.setApplication(application);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprover(approver);
        approval.setAction(request.getAction());
        approval.setComment(request.getComment());
        approval.setApprovedAt(LocalDateTime.now());
        
        approvalRepository.save(approval);
        log.info("审批记录已保存: approvalId={}", approval.getId());
        
        // 6. 更新申请状态
        updateApplicationStatus(application, approvalLevel, request.getAction());
        applicationRepository.save(application);
        
        log.info("审批完成: applicationId={}, level={}, action={}, status={}", 
                applicationId, approvalLevel, request.getAction(), application.getStatus());
        
        return DtoConverter.toScholarshipApplicationDTO(application);
    }
    
    /**
     * 查询审批历史
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipApprovalDTO> getApprovalHistory(Long applicationId) {
        log.debug("查询审批历史: applicationId={}", applicationId);
        
        List<ScholarshipApproval> approvals = approvalRepository.findByApplicationIdWithApprover(applicationId);
        
        return approvals.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询审批流程
     */
    @Override
    @Transactional(readOnly = true)
    public ApprovalFlowDTO getApprovalFlow(Long applicationId, Long userId) {
        log.debug("查询审批流程: applicationId={}, userId={}", applicationId, userId);
        
        ScholarshipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_FOUND));
        
        List<ScholarshipApprovalDTO> history = getApprovalHistory(applicationId);
        Integer currentLevel = getCurrentApprovalLevel(applicationId);
        boolean canApprove = canApprove(applicationId, userId);
        
        String nextApproverRole = getNextApproverRole(application.getStatus());
        
        return ApprovalFlowDTO.builder()
                .applicationId(applicationId)
                .currentLevel(currentLevel)
                .totalLevels(MAX_APPROVAL_LEVEL)
                .approvalHistory(history)
                .canApprove(canApprove)
                .nextApproverRole(nextApproverRole)
                .build();
    }
    
    /**
     * 检查用户是否可以审批
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canApprove(Long applicationId, Long userId) {
        ScholarshipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_FOUND));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 根据申请状态和用户角色判断
        ApplicationStatus status = application.getStatus();
        User.UserRole role = user.getRole();
        
        // 待审核状态，辅导员可以审批
        if (status == ApplicationStatus.PENDING && role == User.UserRole.TEACHER) {
            // 检查是否是学生的辅导员（这里简化处理，实际应该检查是否是该学生的辅导员）
            return !approvalRepository.existsByApplicationIdAndApprovalLevel(applicationId, COUNSELOR_LEVEL);
        }
        
        // 辅导员已批准状态，院系管理员可以审批
        if (status == ApplicationStatus.COUNSELOR_APPROVED && role == User.UserRole.ADMIN) {
            return !approvalRepository.existsByApplicationIdAndApprovalLevel(applicationId, DEPARTMENT_LEVEL);
        }
        
        return false;
    }
    
    /**
     * 获取当前审批级别
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentApprovalLevel(Long applicationId) {
        List<ScholarshipApproval> approvals = approvalRepository.findByApplicationIdWithApprover(applicationId);
        
        if (approvals.isEmpty()) {
            return 0;
        }
        
        // 找到最高的审批级别
        return approvals.stream()
                .map(ScholarshipApproval::getApprovalLevel)
                .max(Integer::compareTo)
                .orElse(0);
    }
    
    /**
     * 验证申请状态
     */
    private void validateApplicationStatus(ScholarshipApplication application, Integer approvalLevel) {
        ApplicationStatus status = application.getStatus();
        
        // 已拒绝的申请不能再审批
        if (status == ApplicationStatus.REJECTED) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_REJECTED);
        }
        
        // 院系已批准的申请不能再审批
        if (status == ApplicationStatus.DEPT_APPROVED) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_ALREADY_APPROVED);
        }
        
        // 辅导员审批时，申请必须是待审核状态
        if (approvalLevel == COUNSELOR_LEVEL && status != ApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_STATUS_INVALID);
        }
        
        // 院系审批时，申请必须是辅导员已批准状态
        if (approvalLevel == DEPARTMENT_LEVEL && status != ApplicationStatus.COUNSELOR_APPROVED) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_STATUS_INVALID);
        }
    }
    
    /**
     * 更新申请状态
     */
    private void updateApplicationStatus(
            ScholarshipApplication application, Integer approvalLevel, ApprovalAction action) {
        
        if (action == ApprovalAction.REJECT) {
            // 拒绝
            application.setStatus(ApplicationStatus.REJECTED);
        } else if (action == ApprovalAction.APPROVE) {
            // 批准
            if (approvalLevel == COUNSELOR_LEVEL) {
                application.setStatus(ApplicationStatus.COUNSELOR_APPROVED);
            } else if (approvalLevel == DEPARTMENT_LEVEL) {
                application.setStatus(ApplicationStatus.DEPT_APPROVED);
            }
        }
    }
    
    /**
     * 获取审批级别描述
     */
    private String getApprovalLevelDescription(Integer level) {
        return switch (level) {
            case 1 -> "辅导员";
            case 2 -> "院系";
            default -> "未知";
        };
    }
    
    /**
     * 获取下一审批人角色
     */
    private String getNextApproverRole(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "辅导员";
            case COUNSELOR_APPROVED -> "院系管理员";
            case DEPT_APPROVED -> "审批完成";
            case REJECTED -> "已拒绝";
        };
    }
    
    /**
     * 转换为DTO
     */
    private ScholarshipApprovalDTO convertToDTO(ScholarshipApproval approval) {
        return ScholarshipApprovalDTO.builder()
                .id(approval.getId())
                .applicationId(approval.getApplication().getId())
                .approvalLevel(approval.getApprovalLevel())
                .approvalLevelDescription(getApprovalLevelDescription(approval.getApprovalLevel()))
                .approverId(approval.getApprover().getId())
                .approverName(approval.getApprover().getUsername())
                .action(approval.getAction())
                .actionDescription(approval.getAction().getDescription())
                .comment(approval.getComment())
                .approvedAt(approval.getApprovedAt())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}

