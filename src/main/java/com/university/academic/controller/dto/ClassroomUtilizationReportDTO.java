package com.university.academic.controller.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 教室使用率报告DTO
 */
@Data
public class ClassroomUtilizationReportDTO {
    
    /**
     * 报告生成时间
     */
    private LocalDate reportDate;
    
    /**
     * 统计开始日期
     */
    private LocalDate startDate;
    
    /**
     * 统计结束日期
     */
    private LocalDate endDate;
    
    /**
     * 统计天数
     */
    private Integer totalDays;
    
    /**
     * 教室总数
     */
    private Integer totalClassrooms;
    
    /**
     * 平均使用率
     */
    private Double averageUtilizationRate;
    
    /**
     * 最高使用率
     */
    private Double maxUtilizationRate;
    
    /**
     * 最低使用率
     */
    private Double minUtilizationRate;
    
    /**
     * 高使用率教室数量（>80%）
     */
    private Integer highUtilizationCount;
    
    /**
     * 正常使用率教室数量（60%-90%）
     */
    private Integer normalUtilizationCount;
    
    /**
     * 中等使用率教室数量（30%-60%）
     */
    private Integer mediumUtilizationCount;
    
    /**
     * 低使用率教室数量（<30%）
     */
    private Integer lowUtilizationCount;
    
    /**
     * 各教室使用率详情
     */
    private List<ClassroomUtilizationDTO> classroomUtilizations;
    
    /**
     * 高使用率教室列表（>80%）
     */
    private List<ClassroomUtilizationDTO> highUtilizationClassrooms;
    
    /**
     * 低使用率教室列表（<30%）
     */
    private List<ClassroomUtilizationDTO> lowUtilizationClassrooms;
    
    /**
     * 按楼栋统计
     */
    private List<BuildingUtilization> buildingUtilizations;
    
    /**
     * 按类型统计
     */
    private List<TypeUtilization> typeUtilizations;
    
    /**
     * 楼栋使用率统计
     */
    @Data
    public static class BuildingUtilization {
        /**
         * 楼栋名称
         */
        private String building;
        
        /**
         * 教室数量
         */
        private Integer classroomCount;
        
        /**
         * 平均使用率
         */
        private Double avgUtilizationRate;
        
        /**
         * 总使用时长（小时）
         */
        private Double totalUsageHours;
    }
    
    /**
     * 类型使用率统计
     */
    @Data
    public static class TypeUtilization {
        /**
         * 教室类型
         */
        private String type;
        
        /**
         * 类型描述
         */
        private String typeDescription;
        
        /**
         * 教室数量
         */
        private Integer classroomCount;
        
        /**
         * 平均使用率
         */
        private Double avgUtilizationRate;
        
        /**
         * 总使用时长（小时）
         */
        private Double totalUsageHours;
    }
}

