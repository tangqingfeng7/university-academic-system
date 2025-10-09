package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考场统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatisticsDTO {
    
    /**
     * 学期ID
     */
    private Long semesterId;
    
    /**
     * 学期名称
     */
    private String semesterName;
    
    /**
     * 考场总数
     */
    private Long totalRooms;
    
    /**
     * 总容量
     */
    private Integer totalCapacity;
    
    /**
     * 已使用容量
     */
    private Integer usedCapacity;
    
    /**
     * 剩余容量
     */
    private Integer remainingCapacity;
    
    /**
     * 使用率（百分比）
     */
    private Double utilizationRate;
}

