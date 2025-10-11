package com.university.academic.dto;

import com.university.academic.entity.ScholarshipLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建奖学金请求
 */
@Data
public class CreateScholarshipRequest {
    
    /**
     * 奖学金名称
     */
    @NotBlank(message = "奖学金名称不能为空")
    @Size(max = 100, message = "奖学金名称长度不能超过100个字符")
    private String name;
    
    /**
     * 奖学金等级
     */
    @NotNull(message = "奖学金等级不能为空")
    private ScholarshipLevel level;
    
    /**
     * 奖学金金额
     */
    @NotNull(message = "奖学金金额不能为空")
    @Positive(message = "奖学金金额必须大于0")
    private Double amount;
    
    /**
     * 名额
     */
    @NotNull(message = "名额不能为空")
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
}

