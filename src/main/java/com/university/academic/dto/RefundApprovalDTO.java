package com.university.academic.dto;

import com.university.academic.entity.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 退费审批记录DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundApprovalDTO {

    /**
     * 审批ID
     */
    private Long id;

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
     * 操作描述
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

