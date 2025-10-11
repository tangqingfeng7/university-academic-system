package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖学金评定规则DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRuleDTO {
    
    /**
     * 学业成绩权重（0-1之间）
     */
    private Double academicWeight;
    
    /**
     * 综合素质权重（0-1之间）
     */
    private Double comprehensiveWeight;
    
    /**
     * 获奖加分比例（0-1之间）
     */
    private Double awardBonus;
    
    /**
     * 社会实践加分比例（0-1之间）
     */
    private Double practiceBonus;
    
    /**
     * 最低GPA要求
     */
    private Double minGpa;
    
    /**
     * 最低学分要求
     */
    private Double minCredits;
    
    /**
     * 是否要求无处分记录
     */
    private Boolean requireCleanRecord;
    
    /**
     * 是否要求按时缴费
     */
    private Boolean requirePaidTuition;
}

