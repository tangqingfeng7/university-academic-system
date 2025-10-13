package com.university.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排课项DTO
 * 单个课程的排课信息
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDTO {

    /**
     * 课程开课ID
     */
    private Long courseOfferingId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教室ID（前端筛选用）
     */
    private Long classroomId;
    
    /**
     * 教室编号
     */
    private String classroomNo;
    
    /**
     * 教室名称（前端筛选用）
     */
    private String classroomName;

    /**
     * 星期几（1-7）
     */
    private Integer dayOfWeek;

    /**
     * 星期描述
     */
    private String dayOfWeekDescription;

    /**
     * 开始时段（1-8）
     */
    private Integer startSlot;

    /**
     * 结束时段（1-8）
     */
    private Integer endSlot;

    /**
     * 时段描述
     */
    private String timeSlotDescription;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /**
     * 是否满足所有硬约束
     */
    private Boolean satisfiesHardConstraints;

    /**
     * 软约束满足度（0-100）
     */
    private Double softConstraintScore;
    
    /**
     * 时间段列表（前端兼容字段）
     */
    private java.util.List<TimeSlotInfo> timeSlots;
    
    /**
     * 时间段信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimeSlotInfo {
        private Integer dayOfWeek;
        private Integer timeSlot;
    }
}

