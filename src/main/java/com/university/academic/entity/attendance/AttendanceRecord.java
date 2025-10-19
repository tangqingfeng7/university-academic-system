package com.university.academic.entity.attendance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤记录实体类
 * 记录每次考勤的基本信息和统计数据
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_record", indexes = {
    @Index(name = "idx_offering_date", columnList = "offering_id, attendance_date"),
    @Index(name = "idx_teacher_date", columnList = "teacher_id, attendance_date"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(value = {"details", "hibernateLazyInitializer", "handler"}, allowSetters = true)
public class AttendanceRecord extends BaseEntity {

    /**
     * 开课计划
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    @JsonIgnoreProperties({"selections", "grades", "schedules", "exams", "evaluations", "hibernateLazyInitializer", "handler"})
    private CourseOffering offering;

    /**
     * 授课教师
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"offerings", "user", "hibernateLazyInitializer", "handler"})
    private Teacher teacher;

    /**
     * 考勤日期
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /**
     * 考勤时间
     */
    @Column(name = "attendance_time", nullable = false)
    private LocalTime attendanceTime;

    /**
     * 考勤方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private AttendanceMethod method;

    /**
     * 记录状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.IN_PROGRESS;

    /**
     * 应到人数
     */
    @Column(name = "total_students")
    private Integer totalStudents;

    /**
     * 实到人数
     */
    @Column(name = "present_count")
    @Builder.Default
    private Integer presentCount = 0;

    /**
     * 缺勤人数
     */
    @Column(name = "absent_count")
    @Builder.Default
    private Integer absentCount = 0;

    /**
     * 迟到人数
     */
    @Column(name = "late_count")
    @Builder.Default
    private Integer lateCount = 0;

    /**
     * 请假人数
     */
    @Column(name = "leave_count")
    @Builder.Default
    private Integer leaveCount = 0;

    /**
     * 出勤率
     */
    @Column(name = "attendance_rate")
    private Double attendanceRate;

    /**
     * 二维码令牌（用于扫码签到）
     */
    @Column(name = "qr_token", length = 100)
    private String qrToken;

    /**
     * 二维码过期时间
     */
    @Column(name = "qr_expire_time")
    private LocalDateTime qrExpireTime;

    /**
     * 纬度（用于定位签到）
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * 经度（用于定位签到）
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * 地理围栏半径（米）
     */
    @Column(name = "geofence_radius")
    private Integer geofenceRadius;

    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * 考勤明细列表
     */
    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AttendanceDetail> details = new ArrayList<>();
}

