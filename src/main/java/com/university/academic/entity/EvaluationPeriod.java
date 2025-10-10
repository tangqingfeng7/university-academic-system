package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 评价周期实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "evaluation_period")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationPeriod extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 检查当前时间是否在评价周期内
     */
    public boolean isInPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startTime) && now.isBefore(endTime);
    }

    /**
     * 检查评价周期是否已结束
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }
}

