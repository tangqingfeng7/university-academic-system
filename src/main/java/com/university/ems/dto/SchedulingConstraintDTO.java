package com.university.ems.dto;

import com.university.ems.enums.ConstraintType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排课约束DTO
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingConstraintDTO {

    /**
     * 约束ID
     */
    private Long id;

    /**
     * 约束名称
     */
    private String name;

    /**
     * 约束类型
     */
    private ConstraintType type;

    /**
     * 约束类型描述
     */
    private String typeDescription;

    /**
     * 约束描述
     */
    private String description;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 是否启用
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

