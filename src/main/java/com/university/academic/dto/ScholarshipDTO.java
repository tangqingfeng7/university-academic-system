package com.university.academic.dto;

import com.university.academic.entity.ScholarshipLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 奖学金DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDTO {
    
    private Long id;
    
    /**
     * 奖学金名称
     */
    private String name;
    
    /**
     * 奖学金等级
     */
    private ScholarshipLevel level;
    
    /**
     * 等级描述
     */
    private String levelDescription;
    
    /**
     * 奖学金金额
     */
    private Double amount;
    
    /**
     * 名额
     */
    private Integer quota;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 最低GPA要求
     */
    private Double minGpa;
    
    /**
     * 最低学分要求
     */
    private Double minCredits;
    
    /**
     * 其他要求
     */
    private String requirements;
    
    /**
     * 是否启用
     */
    private Boolean active;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

