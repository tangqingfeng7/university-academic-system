package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教室使用记录实体类
 */
@Entity
@Table(name = "classroom_usage_log", indexes = {
    @Index(name = "idx_usage_classroom", columnList = "classroom_id"),
    @Index(name = "idx_usage_time", columnList = "start_time, end_time"),
    @Index(name = "idx_usage_type", columnList = "type")
})
@Data
public class ClassroomUsageLog {
    
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
     * 使用类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UsageType type;
    
    /**
     * 关联ID（课程ID、考试ID、借用ID等）
     */
    @Column(name = "reference_id")
    private Long referenceId;
    
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
     * 使用说明
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 创建前自动设置时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

