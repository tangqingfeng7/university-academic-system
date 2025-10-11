package com.university.academic.service;

import com.university.academic.dto.ApprovalFlowDTO;
import com.university.academic.dto.ApprovalRequest;
import com.university.academic.dto.ScholarshipApplicationDTO;
import com.university.academic.dto.ScholarshipApprovalDTO;

import java.util.List;

/**
 * 奖学金审批服务接口
 */
public interface ScholarshipApprovalService {
    
    /**
     * 辅导员审核
     *
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @param request 审批请求
     * @return 申请DTO
     */
    ScholarshipApplicationDTO counselorApprove(Long applicationId, Long approverId, ApprovalRequest request);
    
    /**
     * 院系审批
     *
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @param request 审批请求
     * @return 申请DTO
     */
    ScholarshipApplicationDTO departmentApprove(Long applicationId, Long approverId, ApprovalRequest request);
    
    /**
     * 通用审批方法
     *
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @param approvalLevel 审批级别
     * @param request 审批请求
     * @return 申请DTO
     */
    ScholarshipApplicationDTO approve(Long applicationId, Long approverId, Integer approvalLevel, ApprovalRequest request);
    
    /**
     * 查询审批历史
     *
     * @param applicationId 申请ID
     * @return 审批记录列表
     */
    List<ScholarshipApprovalDTO> getApprovalHistory(Long applicationId);
    
    /**
     * 查询审批流程
     *
     * @param applicationId 申请ID
     * @param userId 当前用户ID
     * @return 审批流程DTO
     */
    ApprovalFlowDTO getApprovalFlow(Long applicationId, Long userId);
    
    /**
     * 检查用户是否可以审批
     *
     * @param applicationId 申请ID
     * @param userId 用户ID
     * @return 是否可以审批
     */
    boolean canApprove(Long applicationId, Long userId);
    
    /**
     * 获取当前审批级别
     *
     * @param applicationId 申请ID
     * @return 当前审批级别
     */
    Integer getCurrentApprovalLevel(Long applicationId);
}

