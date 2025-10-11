package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 奖学金获奖记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipAwardDTO {
    
    private Long id;
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 院系名称
     */
    private String departmentName;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 年级
     */
    private Integer grade;
    
    /**
     * 奖学金ID
     */
    private Long scholarshipId;
    
    /**
     * 奖学金名称
     */
    private String scholarshipName;
    
    /**
     * 奖学金等级
     */
    private String scholarshipLevel;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 奖学金金额
     */
    private Double amount;
    
    /**
     * GPA
     */
    private Double gpa;
    
    /**
     * 综合得分
     */
    private Double comprehensiveScore;
    
    /**
     * 获奖时间
     */
    private LocalDateTime awardedAt;
    
    /**
     * 是否已公示
     */
    private Boolean published;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

