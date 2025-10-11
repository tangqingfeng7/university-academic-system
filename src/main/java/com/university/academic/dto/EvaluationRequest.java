package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动评定请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
    
    /**
     * 奖学金ID
     */
    private Long scholarshipId;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 评定规则（可选，不提供则使用默认规则）
     */
    private EvaluationRuleDTO rule;
}

