package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 创建班级请求DTO
 *
 * @author Academic System Team
 */
@Data
public class CreateClassRequest {

    @NotBlank(message = "班级代码不能为空")
    private String classCode;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    @NotNull(message = "入学年份不能为空")
    private Integer enrollmentYear;

    @Min(value = 1, message = "班级容量必须大于0")
    private Integer capacity;

    private Long counselorId;

    private String remarks;
}

