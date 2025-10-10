package com.university.academic.controller.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 教室课表DTO（一周的课表）
 */
@Data
public class ClassroomScheduleDTO {
    
    /**
     * 教室信息
     */
    private ClassroomDTO classroom;
    
    /**
     * 开始日期（周一）
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（周日）
     */
    private LocalDate endDate;
    
    /**
     * 按星期分组的课表
     * Key: 星期几(1-7)，Value: 该天的使用记录列表
     */
    private Map<Integer, List<ClassroomUsageItemDTO>> weeklySchedule;
    
    /**
     * 时间槽配置（用于前端展示）
     */
    private List<TimeSlot> timeSlots;
    
    /**
     * 时间槽
     */
    @Data
    public static class TimeSlot {
        /**
         * 时间段描述（如："08:00-09:40"）
         */
        private String time;
        
        /**
         * 开始时间（分钟，从0:00开始计算）
         */
        private Integer startMinute;
        
        /**
         * 结束时间（分钟，从0:00开始计算）
         */
        private Integer endMinute;
        
        /**
         * 时间槽名称（如："第1-2节"）
         */
        private String name;
    }
}

