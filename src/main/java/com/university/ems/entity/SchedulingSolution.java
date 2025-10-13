package com.university.ems.entity;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Semester;
import com.university.ems.enums.SolutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 排课方案实体类
 * 记录每次排课的方案及其质量评估
 * 
 * @author Academic System Team
 */
@Entity
@Table(name = "scheduling_solution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingSolution extends BaseEntity {

    /**
     * 所属学期
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    /**
     * 方案名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 方案质量分数（0-100）
     * 根据约束满足程度和优化目标计算
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    /**
     * 冲突数量
     * 统计硬约束和软约束的违反次数
     */
    @Column(name = "conflict_count")
    private Integer conflictCount;

    /**
     * 方案状态：DRAFT（草稿）、OPTIMIZING（优化中）、COMPLETED（已完成）、APPLIED（已应用）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolutionStatus status;

    /**
     * 方案生成时间
     */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    /**
     * 方案应用时间
     */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
}

