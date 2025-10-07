package com.university.academic.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新专业请求DTO
 *
 * @author Academic System Team
 */
@Data
public class UpdateMajorRequest {

    @Size(max = 20, message = "专业代码长度不能超过20")
    private String code;

    @Size(max = 100, message = "专业名称长度不能超过100")
    private String name;

    /**
     * 院系ID
     */
    private Long departmentId;
}

