package com.university.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师排课偏好DTO
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPreferenceDTO {

    /**
     * 偏好ID
     */
    private Long id;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 偏好上课的星期几（原始字符串）
     */
    private String preferredDays;

    /**
     * 偏好星期列表（解析后的数组）
     */
    private List<Integer> preferredDaysList;

    /**
     * 偏好星期描述（中文）
     */
    private String preferredDaysDescription;

    /**
     * 偏好的时段（原始字符串）
     */
    private String preferredTimeSlots;

    /**
     * 偏好时段列表（解析后的数组）
     */
    private List<Integer> preferredTimeSlotsList;

    /**
     * 偏好时段描述（中文）
     */
    private String preferredTimeSlotsDescription;

    /**
     * 每天最多上课小时数
     */
    private Integer maxDailyHours;

    /**
     * 每周最多上课小时数
     */
    private Integer maxWeeklyHours;

    /**
     * 备注说明
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

