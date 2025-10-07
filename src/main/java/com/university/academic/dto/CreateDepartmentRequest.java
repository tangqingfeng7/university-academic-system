package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建院系请求DTO
 *
 * @author Academic System Team
 */
@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "院系代码不能为空")
    @Size(max = 20, message = "院系代码长度不能超过20")
    private String code;

    @NotBlank(message = "院系名称不能为空")
    @Size(max = 100, message = "院系名称长度不能超过100")
    private String name;
}

