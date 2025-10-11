package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 综合得分详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveScoreDTO {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * GPA
     */
    private Double gpa;
    
    /**
     * 学业成绩分数
     */
    private Double academicScore;
    
    /**
     * 综合素质分数
     */
    private Double comprehensiveScore;
    
    /**
     * 获奖加分
     */
    private Double awardBonus;
    
    /**
     * 社会实践加分
     */
    private Double practiceBonus;
    
    /**
     * 总分
     */
    private Double totalScore;
    
    /**
     * 排名
     */
    private Integer ranking;
    
    /**
     * 是否推荐
     */
    private Boolean recommended;
    
    /**
     * 不推荐原因
     */
    private String excludeReason;
}

