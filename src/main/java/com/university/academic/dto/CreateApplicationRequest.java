package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建奖学金申请请求
 */
@Data
public class CreateApplicationRequest {
    
    /**
     * 奖学金ID
     */
    @NotNull(message = "奖学金ID不能为空")
    private Long scholarshipId;
    
    /**
     * 学年
     */
    @NotBlank(message = "学年不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "学年格式不正确，应为：2024-2025")
    private String academicYear;
    
    /**
     * 个人陈述
     */
    @NotBlank(message = "个人陈述不能为空")
    private String personalStatement;
    
    /**
     * 证明材料URL（可选）
     */
    private String attachmentUrl;
}

