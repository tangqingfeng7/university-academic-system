package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调课申请实体
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course_change_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请教师ID
     */
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    /**
     * 申请教师姓名
     */
    @Column(name = "teacher_name", nullable = false, length = 50)
    private String teacherName;

    /**
     * 开课计划ID
     */
    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    /**
     * 课程名称
     */
    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    /**
     * 原时间安排（JSON格式）
     */
    @Column(name = "original_schedule", nullable = false, columnDefinition = "TEXT")
    private String originalSchedule;

    /**
     * 新时间安排（JSON格式）
     */
    @Column(name = "new_schedule", nullable = false, columnDefinition = "TEXT")
    private String newSchedule;

    /**
     * 调课原因
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    /**
     * 审批人ID
     */
    @Column(name = "approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Column(name = "approver_name", length = 50)
    private String approverName;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 申请状态枚举
     */
    public enum RequestStatus {
        PENDING,    // 待审批
        APPROVED,   // 已通过
        REJECTED    // 已拒绝
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

