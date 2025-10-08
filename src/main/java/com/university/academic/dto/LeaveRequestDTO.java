package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    /**
     * 请假ID
     */
    private Long id;

    /**
     * 申请人类型
     */
    private String applicantType;

    /**
     * 申请人类型描述
     */
    private String applicantTypeDescription;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人学号/工号
     */
    private String applicantNo;

    /**
     * 请假类型
     */
    private String leaveType;

    /**
     * 请假类型描述
     */
    private String leaveTypeDescription;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 请假天数
     */
    private Integer days;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 审批状态
     */
    private String status;

    /**
     * 审批状态描述
     */
    private String statusDescription;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

