package com.university.ems.entity;

import com.university.academic.entity.Classroom;
import com.university.academic.entity.CourseOffering;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排课结果实体类
 * 存储排课方案的详细排课结果
 * 
 * @author Academic System Team
 */
@Entity
@Table(name = "schedule_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 排课方案
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private SchedulingSolution solution;
    
    /**
     * 课程开课
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;
    
    /**
     * 教室
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    /**
     * 星期（1-7，1为周一）
     */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;
    
    /**
     * 开始时段（1-5）
     */
    @Column(name = "start_slot", nullable = false)
    private Integer startSlot;
    
    /**
     * 结束时段（1-5）
     */
    @Column(name = "end_slot", nullable = false)
    private Integer endSlot;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

