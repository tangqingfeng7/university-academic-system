package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学籍异动实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "student_status_change", indexes = {
    @Index(name = "idx_status_change_student", columnList = "student_id"),
    @Index(name = "idx_status_change_type", columnList = "type"),
    @Index(name = "idx_status_change_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentStatusChange extends BaseEntity {

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 异动类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeType type;

    /**
     * 异动原因
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 开始日期（适用于休学、复学）
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 结束日期（适用于休学）
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 目标专业ID（适用于转专业）
     */
    @Column(name = "target_major_id")
    private Long targetMajorId;

    /**
     * 证明材料附件URL
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * 审批状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    /**
     * 当前审批人ID
     */
    @Column(name = "current_approver_id")
    private Long currentApproverId;

    /**
     * 当前审批级别
     * 1-辅导员，2-院系，3-教务处
     */
    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    /**
     * 审批截止时间
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * 是否已超时
     */
    @Column(name = "is_overdue", nullable = false)
    @Builder.Default
    private Boolean isOverdue = false;

    /**
     * 审批记录
     */
    @OneToMany(mappedBy = "statusChange", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StatusChangeApproval> approvals = new ArrayList<>();

    /**
     * 是否已删除
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}

