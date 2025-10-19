package com.university.academic.vo.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤统计视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatisticsVO {

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

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
     * 总课次
     */
    private Integer totalClasses;

    /**
     * 出勤次数
     */
    private Integer presentCount;

    /**
     * 迟到次数
     */
    private Integer lateCount;

    /**
     * 早退次数
     */
    private Integer earlyLeaveCount;

    /**
     * 请假次数
     */
    private Integer leaveCount;

    /**
     * 旷课次数
     */
    private Integer absentCount;

    /**
     * 出勤率
     */
    private Double attendanceRate;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
}

