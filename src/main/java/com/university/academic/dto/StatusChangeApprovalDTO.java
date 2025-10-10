package com.university.academic.dto;

import com.university.academic.entity.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学籍异动审批记录DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeApprovalDTO {

    /**
     * 审批记录ID
     */
    private Long id;

    /**
     * 异动申请ID
     */
    private Long statusChangeId;

    /**
     * 审批级别
     */
    private Integer approvalLevel;

    /**
     * 审批级别描述
     */
    private String approvalLevelDescription;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批操作
     */
    private ApprovalAction action;

    /**
     * 审批操作描述
     */
    private String actionDescription;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;
}

