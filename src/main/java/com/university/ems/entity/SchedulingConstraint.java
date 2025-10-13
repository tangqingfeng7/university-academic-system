package com.university.ems.entity;

import com.university.academic.entity.BaseEntity;
import com.university.ems.enums.ConstraintType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 排课约束实体类
 * 定义排课过程中需要满足的各种约束条件
 * 
 * @author Academic System Team
 */
@Entity
@Table(name = "scheduling_constraint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingConstraint extends BaseEntity {

    /**
     * 约束名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 约束类型：HARD（硬约束）、SOFT（软约束）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConstraintType type;

    /**
     * 约束描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 权重（用于软约束的优先级排序）
     * 值越大优先级越高
     */
    @Column(nullable = false)
    private Integer weight;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean active;
}

