package com.university.ems.entity;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

/**
 * 教师排课偏好实体类
 * 记录教师对上课时间、课程安排的偏好设置
 * 
 * @author Academic System Team
 */
@Entity
@Table(name = "teacher_preference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherPreference extends BaseEntity {

    /**
     * 教师
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * 偏好上课的星期几
     * 格式：1,2,3,4,5（周一到周五）
     * 例如："1,3,5" 表示偏好周一、周三、周五上课
     */
    @Column(name = "preferred_days", length = 50)
    private String preferredDays;

    /**
     * 偏好的时段
     * 格式：1,2,3,4,5（第1-5节课）
     * 例如："1,2,3" 表示偏好上午的课程
     */
    @Column(name = "preferred_time_slots", length = 100)
    private String preferredTimeSlots;

    /**
     * 每天最多上课小时数
     * 用于控制教师的工作强度
     */
    @Column(name = "max_daily_hours")
    private Integer maxDailyHours;

    /**
     * 每周最多上课小时数
     * 用于控制教师的整体工作量
     */
    @Column(name = "max_weekly_hours")
    private Integer maxWeeklyHours;

    /**
     * 备注说明
     * 记录教师的其他特殊要求或偏好
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
}

