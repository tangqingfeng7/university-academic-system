package com.university.ems.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新教师排课偏好请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class UpdatePreferenceRequest {

    /**
     * 偏好上课的星期几
     */
    @Pattern(regexp = "^[1-7](,[1-7])*$|^$", message = "偏好星期格式错误，应为1-7的数字，用逗号分隔")
    private String preferredDays;

    /**
     * 偏好的时段
     */
    @Pattern(regexp = "^[1-8](,[1-8])*$|^$", message = "偏好时段格式错误，应为1-8的数字，用逗号分隔")
    private String preferredTimeSlots;

    /**
     * 每天最多上课小时数
     */
    @Min(value = 1, message = "每天最多上课小时数不能小于1")
    @Max(value = 12, message = "每天最多上课小时数不能大于12")
    private Integer maxDailyHours;

    /**
     * 每周最多上课小时数
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

