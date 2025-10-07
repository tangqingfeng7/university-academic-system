package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建专业请求DTO
 *
 * @author Academic System Team
 */
@Data
public class CreateMajorRequest {

    @NotBlank(message = "专业代码不能为空")
    @Size(max = 20, message = "专业代码长度不能超过20")
    private String code;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String name;

    @NotNull(message = "院系ID不能为空")
    private Long departmentId;
}

