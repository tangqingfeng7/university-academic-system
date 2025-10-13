package com.university.academic.entity;

import com.university.academic.entity.enums.AppealResult;
import com.university.academic.entity.enums.AppealStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处分申诉实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "discipline_appeal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineAppeal {

    /**
     * 申诉ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 处分记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_id", nullable = false)
    private StudentDiscipline discipline;

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 申诉理由
     */
    @Column(name = "appeal_reason", nullable = false, columnDefinition = "TEXT")
    private String appealReason;

    /**
     * 申诉证据
     */
    @Column(columnDefinition = "TEXT")
    private String evidence;

    /**
     * 附件URL
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * 申诉状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AppealStatus status;

    /**
     * 审核结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", length = 50)
    private AppealResult reviewResult;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 审核人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AppealStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

