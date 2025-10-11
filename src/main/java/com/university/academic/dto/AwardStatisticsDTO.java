package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 获奖统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardStatisticsDTO {
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 总获奖人数
     */
    private Integer totalAwardees;
    
    /**
     * 总奖学金金额
     */
    private Double totalAmount;
    
    /**
     * 按院系统计
     * Key: 院系名称, Value: 获奖人数
     */
    private Map<String, Integer> byDepartment;
    
    /**
     * 按专业统计
     * Key: 专业名称, Value: 获奖人数
     */
    private Map<String, Integer> byMajor;
    
    /**
     * 按年级统计
     * Key: 年级, Value: 获奖人数
     */
    private Map<Integer, Integer> byGrade;
    
    /**
     * 按奖学金等级统计
     * Key: 等级名称, Value: 获奖人数
     */
    private Map<String, Integer> byLevel;
    
    /**
     * 按奖学金类型统计
     * Key: 奖学金名称, Value: 获奖人数
     */
    private Map<String, Integer> byScholarship;
}

