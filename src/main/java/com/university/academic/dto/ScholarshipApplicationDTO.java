package com.university.academic.dto;

import com.university.academic.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 奖学金申请DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipApplicationDTO {
    
    private Long id;
    
    /**
     * 奖学金信息
     */
    private ScholarshipDTO scholarship;
    
    /**
     * 学生信息
     */
    private StudentDTO student;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * GPA
     */
    private Double gpa;
    
    /**
     * 总学分
     */
    private Double totalCredits;
    
    /**
     * 综合得分
     */
    private Double comprehensiveScore;
    
    /**
     * 个人陈述
     */
    private String personalStatement;
    
    /**
     * 证明材料URL
     */
    private String attachmentUrl;
    
    /**
     * 申请状态
     */
    private ApplicationStatus status;
    
    /**
     * 状态描述
     */
    private String statusDescription;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

