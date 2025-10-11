package com.university.academic.dto;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.tuition.RefundMethod;
import com.university.academic.entity.tuition.RefundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退费申请DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplicationDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 缴费记录ID
     */
    private Long paymentId;

    /**
     * 缴费单号
     */
    private String paymentNo;

    /**
     * 原缴费金额
     */
    private Double paymentAmount;

    /**
     * 退费金额
     */
    private Double refundAmount;

    /**
     * 退费原因
     */
    private String reason;

    /**
     * 退费类型
     */
    private RefundType refundType;

    /**
     * 退费类型描述
     */
    private String refundTypeDescription;

    /**
     * 退费方式
     */
    private RefundMethod refundMethod;

    /**
     * 退费方式描述
     */
    private String refundMethodDescription;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 审批状态
     */
    private ApprovalStatus status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 当前审批级别
     */
    private Integer approvalLevel;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 审批完成时间
     */
    private LocalDateTime approvedAt;

    /**
     * 实际退费时间
     */
    private LocalDateTime refundedAt;

    /**
     * 退费交易号
     */
    private String refundTransactionId;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 审批历史
     */
    private List<RefundApprovalDTO> approvalHistory;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

