package com.university.academic.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求DTO
 *
 * @author Academic System Team
 */
@Data
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "用户名长度必须在3-50位之间")
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * 启用状态
     */
    private Boolean enabled;
}

