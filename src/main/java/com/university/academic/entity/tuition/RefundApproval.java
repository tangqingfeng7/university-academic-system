package com.university.academic.entity.tuition;

import com.university.academic.entity.ApprovalAction;
import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 退费审批记录实体类
 * 记录退费申请的审批历史
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "refund_approval", indexes = {
    @Index(name = "idx_refund_approval_app", columnList = "refund_application_id"),
    @Index(name = "idx_refund_approval_level", columnList = "approval_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundApproval extends BaseEntity {

    /**
     * 退费申请
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_application_id", nullable = false)
    private RefundApplication refundApplication;

    /**
     * 审批级别
     */
    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    /**
     * 审批人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    /**
     * 审批操作
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction action;

    /**
     * 审批意见
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * 审批时间
     */
    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;
}

