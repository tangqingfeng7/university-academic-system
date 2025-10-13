package com.university.ems.dto;

import com.university.ems.enums.SolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排课方案DTO
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingSolutionDTO {

    /**
     * 方案ID
     */
    private Long id;

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 方案名称
     */
    private String name;

    /**
     * 质量分数（0-100）
     */
    private Double qualityScore;

    /**
     * 冲突数量
     */
    private Integer conflictCount;

    /**
     * 方案状态
     */
    private SolutionStatus status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 应用时间
     */
    private LocalDateTime appliedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

