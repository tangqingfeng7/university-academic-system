package com.university.ems.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 排课调整请求DTO
 * 用于手动调整单个课程的排课
 * 
 * @author Academic System Team
 */
@Data
public class ScheduleAdjustmentRequest {

    /**
     * 课程开课ID
     */
    @NotNull(message = "课程开课ID不能为空")
    private Long courseOfferingId;

    /**
     * 教室ID（可选，如果不修改则不传）
     */
    private Long classroomId;

    /**
     * 星期几（1-7）
     */
    @Min(value = 1, message = "星期必须在1-7之间")
    @Max(value = 7, message = "星期必须在1-7之间")
    private Integer dayOfWeek;

    /**
     * 开始时段（1-8）
     */
    @Min(value = 1, message = "时段必须在1-8之间")
    @Max(value = 8, message = "时段必须在1-8之间")
    private Integer startSlot;

    /**
     * 结束时段（1-8）
     */
    @Min(value = 1, message = "时段必须在1-8之间")
    @Max(value = 8, message = "时段必须在1-8之间")
    private Integer endSlot;
}

