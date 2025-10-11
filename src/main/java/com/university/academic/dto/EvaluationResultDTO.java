package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评定结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResultDTO {
    
    /**
     * 奖学金ID
     */
    private Long scholarshipId;
    
    /**
     * 奖学金名称
     */
    private String scholarshipName;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 名额
     */
    private Integer quota;
    
    /**
     * 申请人数
     */
    private Integer totalApplicants;
    
    /**
     * 符合条件人数
     */
    private Integer qualifiedApplicants;
    
    /**
     * 推荐名单
     */
    private List<ComprehensiveScoreDTO> recommendedList;
    
    /**
     * 评定规则
     */
    private EvaluationRuleDTO rule;
}

