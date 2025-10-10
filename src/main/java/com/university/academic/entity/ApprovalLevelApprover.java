package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 审批级别审批人配置实体类
 * 支持一个级别配置多个审批人
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "approval_level_approver", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"approval_level", "user_id"}),
       indexes = {
           @Index(name = "idx_level_approver_level", columnList = "approval_level"),
           @Index(name = "idx_level_approver_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLevelApprover extends BaseEntity {

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 审批人优先级（数字越小优先级越高，用于负载均衡）
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    /**
     * 当前处理中的任务数（用于负载均衡）
     */
    @Column(name = "pending_count", nullable = false)
    @Builder.Default
    private Integer pendingCount = 0;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}

