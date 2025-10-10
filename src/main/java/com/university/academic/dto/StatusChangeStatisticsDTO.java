package com.university.academic.dto;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 学籍异动统计结果DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeStatisticsDTO {

    /**
     * 统计时间段（起）
     */
    private LocalDate startDate;

    /**
     * 统计时间段（止）
     */
    private LocalDate endDate;

    /**
     * 总申请数
     */
    private Long totalCount;

    /**
     * 按类型统计
     * Key: ChangeType, Value: 数量
     */
    private Map<ChangeType, Long> countByType;

    /**
     * 按状态统计
     * Key: ApprovalStatus, Value: 数量
     */
    private Map<ApprovalStatus, Long> countByStatus;

    /**
     * 按月份统计
     * Key: 月份(YYYY-MM), Value: 数量
     */
    private Map<String, Long> countByMonth;

    /**
     * 按专业统计（针对转专业）
     * Key: 专业名称, Value: 数量
     */
    private Map<String, Long> transferCountByMajor;

    /**
     * 待审批数量
     */
    private Long pendingCount;

    /**
     * 已批准数量
     */
    private Long approvedCount;

    /**
     * 已拒绝数量
     */
    private Long rejectedCount;

    /**
     * 已取消数量
     */
    private Long cancelledCount;

    /**
     * 超时申请数量
     */
    private Long overdueCount;

    /**
     * 休学申请数量
     */
    private Long suspensionCount;

    /**
     * 复学申请数量
     */
    private Long resumptionCount;

    /**
     * 转专业申请数量
     */
    private Long transferCount;

    /**
     * 退学申请数量
     */
    private Long withdrawalCount;

    /**
     * 平均审批时长（天）
     */
    private Double averageApprovalDays;

    /**
     * 审批通过率（%）
     */
    private Double approvalRate;
}

