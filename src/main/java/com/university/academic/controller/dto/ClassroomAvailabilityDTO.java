package com.university.academic.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教室可用性检查结果DTO
 */
@Data
public class ClassroomAvailabilityDTO {
    
    /**
     * 教室ID
     */
    private Long classroomId;
    
    /**
     * 教室编号
     */
    private String roomNo;
    
    /**
     * 是否可用
     */
    private Boolean available;
    
    /**
     * 检查的时间范围
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * 冲突原因（如果不可用）
     */
    private String conflictReason;
    
    /**
     * 冲突的时间段列表
     */
    private List<ConflictTimeSlot> conflictTimeSlots;
    
    /**
     * 冲突时间段
     */
    @Data
    public static class ConflictTimeSlot {
        /**
         * 冲突类型（COURSE, EXAM, BOOKING）
         */
        private String type;
        
        /**
         * 开始时间
         */
        private LocalDateTime startTime;
        
        /**
         * 结束时间
         */
        private LocalDateTime endTime;
        
        /**
         * 冲突描述
         */
        private String description;
    }
}

