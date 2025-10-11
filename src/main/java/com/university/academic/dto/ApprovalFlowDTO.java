package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审批流程DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalFlowDTO {
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 当前审批级别
     */
    private Integer currentLevel;
    
    /**
     * 总审批级别数
     */
    private Integer totalLevels;
    
    /**
     * 审批历史
     */
    private List<ScholarshipApprovalDTO> approvalHistory;
    
    /**
     * 是否可以审批
     */
    private Boolean canApprove;
    
    /**
     * 下一审批人角色
     */
    private String nextApproverRole;
}

