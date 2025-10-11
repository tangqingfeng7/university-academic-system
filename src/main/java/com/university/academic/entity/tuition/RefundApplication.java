package com.university.academic.entity.tuition;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 退费申请实体类
 * 记录学生的退费申请信息
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "refund_application", indexes = {
    @Index(name = "idx_refund_student", columnList = "student_id"),
    @Index(name = "idx_refund_payment", columnList = "payment_id"),
    @Index(name = "idx_refund_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundApplication extends BaseEntity {

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 原缴费记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private TuitionPayment payment;

    /**
     * 退费金额
     */
    @Column(name = "refund_amount", nullable = false)
    private Double refundAmount;

    /**
     * 退费原因
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 退费类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    @Builder.Default
    private RefundType refundType = RefundType.FULL;

    /**
     * 退费方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false, length = 20)
    private RefundMethod refundMethod;

    /**
     * 银行账号信息（退费到银行卡时使用）
     */
    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    /**
     * 审批状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /**
     * 当前审批级别
     * 1 - 财务审核
     * 2 - 管理员审批
     */
    @Column(name = "approval_level", nullable = false)
    @Builder.Default
    private Integer approvalLevel = 1;

    /**
     * 当前审批人ID
     */
    @Column(name = "current_approver_id")
    private Long currentApproverId;

    /**
     * 提交时间
     */
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * 审批完成时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 实际退费时间
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /**
     * 退费交易号
     */
    @Column(name = "refund_transaction_id", length = 100)
    private String refundTransactionId;

    /**
     * 附件URL（如证明材料）
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
}

