package com.university.academic.entity.attendance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 考勤明细实体类
 * 记录每个学生的具体考勤情况
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_detail", indexes = {
    @Index(name = "idx_record_student", columnList = "record_id, student_id"),
    @Index(name = "idx_student_status", columnList = "student_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, allowSetters = true)
public class AttendanceDetail extends BaseEntity {

    /**
     * 考勤记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    @JsonIgnoreProperties({"details", "teacher", "hibernateLazyInitializer", "handler"})
    private AttendanceRecord attendanceRecord;

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"user", "major", "courseSelections", "hibernateLazyInitializer", "handler"})
    private Student student;

    /**
     * 考勤状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    /**
     * 签到时间
     */
    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    /**
     * 签到纬度
     */
    @Column(name = "checkin_latitude")
    private Double checkinLatitude;

    /**
     * 签到经度
     */
    @Column(name = "checkin_longitude")
    private Double checkinLongitude;

    /**
     * 是否补签
     */
    @Column(name = "is_makeup", nullable = false)
    @Builder.Default
    private Boolean isMakeup = false;

    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * 修改人ID
     */
    @Column(name = "modified_by")
    private Long modifiedBy;

    /**
     * 修改原因
     */
    @Column(name = "modify_reason", columnDefinition = "TEXT")
    private String modifyReason;
}

