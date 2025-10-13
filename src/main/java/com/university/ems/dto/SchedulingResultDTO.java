package com.university.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排课结果DTO
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingResultDTO {

    /**
     * 方案ID
     */
    private Long solutionId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 质量分数（0-100）
     */
    private Double qualityScore;

    /**
     * 冲突数量
     */
    private Integer conflictCount;

    /**
     * 硬约束违反次数
     */
    private Integer hardConstraintViolations;

    /**
     * 软约束违反次数
     */
    private Integer softConstraintViolations;

    /**
     * 排课的课程数量
     */
    private Integer scheduledCourseCount;

    /**
     * 未排课的课程数量
     */
    private Integer unscheduledCourseCount;

    /**
     * 迭代次数
     */
    private Integer iterations;

    /**
     * 耗时（毫秒）
     */
    private Long elapsedMillis;

    /**
     * 排课详情列表
     */
    private List<ScheduleItemDTO> scheduleItems;
    
    /**
     * 排课详情列表（前端兼容字段）
     */
    private List<ScheduleItemDTO> scheduledItems;

    /**
     * 冲突详情
     */
    private List<ConflictDTO> conflicts;

    /**
     * 统计信息
     */
    private Map<String, Object> statistics;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 错误信息
     */
    private String errorMessage;
}

