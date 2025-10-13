package com.university.ems.dto;

import com.university.ems.enums.ConstraintType;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建排课约束请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class CreateConstraintRequest {

    /**
     * 约束名称
     */
    @NotBlank(message = "约束名称不能为空")
    @Size(max = 100, message = "约束名称长度不能超过100个字符")
    private String name;

    /**
     * 约束类型：HARD（硬约束）、SOFT（软约束）
     */
    @NotNull(message = "约束类型不能为空")
    private ConstraintType type;

    /**
     * 约束描述
     */
    private String description;

    /**
     * 权重（用于软约束的优先级排序，范围1-100）
     */
    @NotNull(message = "权重不能为空")
    @Min(value = 1, message = "权重不能小于1")
    @Max(value = 100, message = "权重不能大于100")
    private Integer weight;

    /**
     * 是否启用
     */
    private Boolean active = true;
}

