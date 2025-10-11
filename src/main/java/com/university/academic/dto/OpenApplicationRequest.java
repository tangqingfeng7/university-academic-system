package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 开启奖学金申请请求
 */
@Data
public class OpenApplicationRequest {
    
    /**
     * 学年
     */
    @NotBlank(message = "学年不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "学年格式不正确，应为：2024-2025")
    private String academicYear;
}

