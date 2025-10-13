package com.university.ems.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 设置教师排课偏好请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class SetPreferenceRequest {

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 偏好上课的星期几
     * 格式：1,2,3,4,5（周一到周五）
     * 例如："1,3,5" 表示偏好周一、周三、周五上课
     */
    @Pattern(regexp = "^[1-7](,[1-7])*$|^$", message = "偏好星期格式错误，应为1-7的数字，用逗号分隔")
    private String preferredDays;

    /**
     * 偏好的时段
     * 格式：1,2,3,4,5,6,7,8（第1-8节课）
     * 例如："1,2,3,4" 表示偏好上午的课程
     */
    @Pattern(regexp = "^[1-8](,[1-8])*$|^$", message = "偏好时段格式错误，应为1-8的数字，用逗号分隔")
    private String preferredTimeSlots;

    /**
     * 每天最多上课小时数（单位：小时）
     */
    @Min(value = 1, message = "每天最多上课小时数不能小于1")
    @Max(value = 12, message = "每天最多上课小时数不能大于12")
    private Integer maxDailyHours;

    /**
     * 每周最多上课小时数（单位：小时）
     */
    @Min(value = 1, message = "每周最多上课小时数不能小于1")
    @Max(value = 40, message = "每周最多上课小时数不能大于40")
    private Integer maxWeeklyHours;

    /**
     * 备注说明
     */
    @Size(max = 500, message = "备注说明长度不能超过500个字符")
    private String notes;
}

