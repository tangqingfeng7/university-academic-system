package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 学籍异动审批记录实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "status_change_approval", indexes = {
    @Index(name = "idx_approval_change", columnList = "status_change_id"),
    @Index(name = "idx_approval_level", columnList = "approval_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChangeApproval extends BaseEntity {

    /**
     * 关联的学籍异动申请
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_change_id", nullable = false)
    private StudentStatusChange statusChange;

    /**
     * 审批级别
     * 1-辅导员，2-院系，3-教务处
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

    /**
     * 是否已删除
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}

