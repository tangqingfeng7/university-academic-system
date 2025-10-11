package com.university.academic.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新奖学金请求
 */
@Data
public class UpdateScholarshipRequest {
    
    /**
     * 奖学金金额
     */
    @Positive(message = "奖学金金额必须大于0")
    private Double amount;
    
    /**
     * 名额
     */
    @Positive(message = "名额必须大于0")
    private Integer quota;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 最低GPA要求
     */
    @DecimalMin(value = "0.0", message = "最低GPA不能小于0")
    @DecimalMax(value = "4.0", message = "最低GPA不能大于4.0")
    private Double minGpa;
    
    /**
     * 最低学分要求
     */
    @DecimalMin(value = "0.0", message = "最低学分不能小于0")
    private Double minCredits;
    
    /**
     * 其他要求
     */
    private String requirements;
    
    /**
     * 是否启用
     */
    private Boolean active;
}

