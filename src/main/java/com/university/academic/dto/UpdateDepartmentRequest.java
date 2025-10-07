package com.university.academic.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新院系请求DTO
 *
 * @author Academic System Team
 */
@Data
public class UpdateDepartmentRequest {

    @Size(max = 20, message = "院系代码长度不能超过20")
    private String code;

    @Size(max = 100, message = "院系名称长度不能超过100")
    private String name;
}

