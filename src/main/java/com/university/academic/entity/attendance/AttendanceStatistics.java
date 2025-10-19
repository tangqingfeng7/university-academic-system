package com.university.academic.entity.attendance;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 考勤统计实体类
 * 缓存学生的考勤汇总数据，提高查询性能
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_statistics", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_offering", columnNames = {"student_id", "offering_id"})
    },
    indexes = {
        @Index(name = "idx_offering_rate", columnList = "offering_id, attendance_rate")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatistics extends BaseEntity {

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 开课计划
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private CourseOffering offering;

    /**
     * 总课次
     */
    @Column(name = "total_classes", nullable = false)
    @Builder.Default
    private Integer totalClasses = 0;

    /**
     * 出勤次数
     */
    @Column(name = "present_count", nullable = false)
    @Builder.Default
    private Integer presentCount = 0;

    /**
     * 迟到次数
     */
    @Column(name = "late_count", nullable = false)
    @Builder.Default
    private Integer lateCount = 0;

    /**
     * 早退次数
     */
    @Column(name = "early_leave_count", nullable = false)
    @Builder.Default
    private Integer earlyLeaveCount = 0;

    /**
     * 请假次数
     */
    @Column(name = "leave_count", nullable = false)
    @Builder.Default
    private Integer leaveCount = 0;

    /**
     * 旷课次数
     */
    @Column(name = "absent_count", nullable = false)
    @Builder.Default
    private Integer absentCount = 0;

    /**
     * 出勤率
     */
    @Column(name = "attendance_rate")
    private Double attendanceRate;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}

