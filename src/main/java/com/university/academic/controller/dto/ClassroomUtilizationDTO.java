package com.university.academic.controller.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 教室使用率DTO
 */
@Data
public class ClassroomUtilizationDTO {
    
    /**
     * 教室信息
     */
    private ClassroomDTO classroom;
    
    /**
     * 统计开始日期
     */
    private LocalDate startDate;
    
    /**
     * 统计结束日期
     */
    private LocalDate endDate;
    
    /**
     * 总使用次数
     */
    private Integer totalUsageCount;
    
    /**
     * 总使用时长（分钟）
     */
    private Long totalUsageMinutes;
    
    /**
     * 总使用时长（小时）
     */
    private Double totalUsageHours;
    
    /**
     * 工作时长（分钟，基于工作时间8:00-22:00）
     */
    private Long workMinutes;
    
    /**
     * 使用率（百分比）
     */
    private Double utilizationRate;
    
    /**
     * 课程使用次数
     */
    private Integer courseUsageCount;
    
    /**
     * 考试使用次数
     */
    private Integer examUsageCount;
    
    /**
     * 借用次数
     */
    private Integer bookingUsageCount;
    
    /**
     * 平均每日使用时长（小时）
     */
    private Double avgDailyUsageHours;
    
    /**
     * 使用率等级（HIGH, NORMAL, LOW）
     */
    private String utilizationLevel;
    
    /**
     * 是否异常（使用率过高或过低）
     */
    private Boolean isAbnormal;
}

