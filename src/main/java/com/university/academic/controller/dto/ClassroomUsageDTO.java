package com.university.academic.controller.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 教室使用情况DTO
 */
@Data
public class ClassroomUsageDTO {
    
    /**
     * 教室信息
     */
    private ClassroomDTO classroom;
    
    /**
     * 查询日期
     */
    private LocalDate date;
    
    /**
     * 使用记录列表（按时间排序）
     */
    private List<ClassroomUsageItemDTO> usageItems;
    
    /**
     * 统计信息
     */
    private UsageStatistics statistics;
    
    /**
     * 使用统计信息
     */
    @Data
    public static class UsageStatistics {
        /**
         * 总使用次数
         */
        private Integer totalCount;
        
        /**
         * 总使用时长（分钟）
         */
        private Long totalMinutes;
        
        /**
         * 课程使用次数
         */
        private Integer courseCount;
        
        /**
         * 考试使用次数
         */
        private Integer examCount;
        
        /**
         * 借用次数
         */
        private Integer bookingCount;
        
        /**
         * 使用率（百分比，基于工作时间8:00-22:00）
         */
        private Double utilizationRate;
    }
}

