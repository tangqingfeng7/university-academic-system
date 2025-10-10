package com.university.academic.dto;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 学籍异动查询条件DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeQueryDTO {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名（模糊查询）
     */
    private String studentName;

    /**
     * 学号（模糊查询）
     */
    private String studentNumber;

    /**
     * 异动类型
     */
    private ChangeType type;

    /**
     * 审批状态
     */
    private ApprovalStatus status;

    /**
     * 开始日期（起）
     */
    private LocalDate startDateFrom;

    /**
     * 开始日期（止）
     */
    private LocalDate startDateTo;

    /**
     * 创建时间（起）
     */
    private LocalDate createdAtFrom;

    /**
     * 创建时间（止）
     */
    private LocalDate createdAtTo;

    /**
     * 专业ID（筛选转专业记录）
     */
    private Long majorId;

    /**
     * 目标专业ID（筛选转专业记录）
     */
    private Long targetMajorId;

    /**
     * 审批级别
     */
    private Integer approvalLevel;

    /**
     * 当前审批人ID
     */
    private Long currentApproverId;

    /**
     * 是否超时
     */
    private Boolean isOverdue;
}

