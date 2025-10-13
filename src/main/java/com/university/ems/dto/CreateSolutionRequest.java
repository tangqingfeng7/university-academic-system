package com.university.ems.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建排课方案请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class CreateSolutionRequest {

    /**
     * 学期ID
     */
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    /**
     * 方案名称
     */
    private String name;

    /**
     * 方案描述
     */
    private String description;
}

