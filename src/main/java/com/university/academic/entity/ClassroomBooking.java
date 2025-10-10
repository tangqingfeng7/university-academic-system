package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教室借用实体类
 */
@Entity
@Table(name = "classroom_booking", indexes = {
    @Index(name = "idx_booking_classroom", columnList = "classroom_id"),
    @Index(name = "idx_booking_applicant", columnList = "applicant_id"),
    @Index(name = "idx_booking_time", columnList = "start_time, end_time"),
    @Index(name = "idx_booking_status", columnList = "status")
})
@Data
public class ClassroomBooking {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 教室
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    /**
     * 申请人ID
     */
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    /**
     * 借用目的
     */
    @Column(nullable = false, length = 200)
    private String purpose;
    
    /**
     * 预计参加人数
     */
    @Column(name = "expected_attendees")
    private Integer expectedAttendees;
    
    /**
     * 借用状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;
    
    /**
     * 审批人ID
     */
    @Column(name = "approved_by")
    private Long approvedBy;
    
    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;
    
    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
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
     * 创建前自动设置时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }
    
    /**
     * 更新前自动设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

