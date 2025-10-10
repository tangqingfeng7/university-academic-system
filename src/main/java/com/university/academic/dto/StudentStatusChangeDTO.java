package com.university.academic.dto;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学籍异动信息DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatusChangeDTO {

    /**
     * 异动ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 异动类型
     */
    private ChangeType type;

    /**
     * 异动类型描述
     */
    private String typeDescription;

    /**
     * 异动原因
     */
    private String reason;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 目标专业ID
     */
    private Long targetMajorId;

    /**
     * 目标专业名称
     */
    private String targetMajorName;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 审批状态
     */
    private ApprovalStatus status;

    /**
     * 审批状态描述
     */
    private String statusDescription;

    /**
     * 当前审批人ID
     */
    private Long currentApproverId;

    /**
     * 当前审批人姓名
     */
    private String currentApproverName;

    /**
     * 审批级别
     */
    private Integer approvalLevel;

    /**
     * 审批级别描述
     */
    private String approvalLevelDescription;

    /**
     * 审批截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否已超时
     */
    private Boolean isOverdue;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 审批历史记录
     */
    private java.util.List<StatusChangeApprovalDTO> approvalHistory;
}

