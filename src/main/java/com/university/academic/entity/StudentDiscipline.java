package com.university.academic.entity;

import com.university.academic.entity.enums.ApprovalStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.entity.enums.DisciplineType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生处分记录实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "student_discipline")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDiscipline {

    /**
     * 处分ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 处分类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "discipline_type", nullable = false, length = 50)
    private DisciplineType disciplineType;

    /**
     * 处分原因
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 详细描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 违纪发生日期
     */
    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    /**
     * 处分日期
     */
    @Column(name = "punishment_date", nullable = false)
    private LocalDate punishmentDate;

    /**
     * 处分状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DisciplineStatus status;

    /**
     * 审批状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 50)
    private ApprovalStatus approvalStatus;

    /**
     * 是否可解除
     */
    @Column(name = "can_remove", nullable = false)
    private Boolean canRemove;

    /**
     * 解除日期
     */
    @Column(name = "removed_date")
    private LocalDate removedDate;

    /**
     * 解除原因
     */
    @Column(name = "removed_reason", columnDefinition = "TEXT")
    private String removedReason;

    /**
     * 解除操作人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "removed_by")
    private User removedBy;

    /**
     * 附件URL
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * 上报人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    /**
     * 审批人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @Column(nullable = false)
    private Boolean deleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = false;
        }
        if (status == null) {
            status = DisciplineStatus.ACTIVE;
        }
        if (canRemove == null) {
            canRemove = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

