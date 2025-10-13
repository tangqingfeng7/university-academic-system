package com.university.ems.dto;

import com.university.ems.enums.ConstraintType;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新排课约束请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class UpdateConstraintRequest {

    /**
     * 约束名称
     */
    @Size(max = 100, message = "约束名称长度不能超过100个字符")
    private String name;

    /**
     * 约束类型
     */
    private ConstraintType type;

    /**
     * 约束描述
     */
    private String description;

    /**
     * 权重（范围1-100）
     */
    @Min(value = 1, message = "权重不能小于1")
    @Max(value = 100, message = "权重不能大于100")
    private Integer weight;

    /**
     * 是否启用
     */
    private Boolean active;
}

