package com.university.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冲突DTO
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictDTO {

    /**
     * 冲突类型
     */
    private String conflictType;

    /**
     * 严重程度：HARD（硬约束）、SOFT（软约束）
     */
    private String severity;

    /**
     * 冲突描述
     */
    private String description;

    /**
     * 涉及的课程ID列表
     */
    private Long[] courseOfferingIds;

    /**
     * 涉及的教师ID
     */
    private Long teacherId;

    /**
     * 涉及的教室ID
     */
    private Long classroomId;

    /**
     * 冲突时间（星期）
     */
    private Integer dayOfWeek;

    /**
     * 冲突时间（时段）
     */
    private Integer timeSlot;
}

