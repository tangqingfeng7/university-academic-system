package com.university.academic.vo.attendance;

import com.university.academic.entity.attendance.AttendanceMethod;
import com.university.academic.entity.attendance.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤记录视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordVO {

    /**
     * 考勤记录ID
     */
    private Long id;

    /**
     * 开课计划ID
     */
    private Long offeringId;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 考勤时间
     */
    private LocalTime attendanceTime;

    /**
     * 考勤方式
     */
    private AttendanceMethod method;

    /**
     * 考勤方式描述
     */
    private String methodDesc;

    /**
     * 记录状态
     */
    private RecordStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 应到人数
     */
    private Integer totalStudents;

    /**
     * 实到人数
     */
    private Integer presentCount;

    /**
     * 缺勤人数
     */
    private Integer absentCount;

    /**
     * 迟到人数
     */
    private Integer lateCount;

    /**
     * 请假人数
     */
    private Integer leaveCount;

    /**
     * 出勤率
     */
    private Double attendanceRate;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

