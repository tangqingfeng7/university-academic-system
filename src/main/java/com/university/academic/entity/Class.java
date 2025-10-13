package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 班级实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "class", indexes = {
    @Index(name = "idx_class_code", columnList = "class_code"),
    @Index(name = "idx_class_major", columnList = "major_id"),
    @Index(name = "idx_class_year", columnList = "enrollment_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class extends BaseEntity {

    /**
     * 班级代码（如：2024CS01）
     */
    @Column(name = "class_code", nullable = false, unique = true, length = 20)
    private String classCode;

    /**
     * 班级名称（如：计算机科学与技术2024级1班）
     */
    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    /**
     * 所属专业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    /**
     * 入学年份
     */
    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    /**
     * 班级人数上限
     */
    @Column(name = "capacity")
    private Integer capacity;

    /**
     * 辅导员ID
     */
    @Column(name = "counselor_id")
    private Long counselorId;

    /**
     * 备注
     */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    /**
     * 是否删除（软删除）
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}

