package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "leave_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    /**
     * 申请人类型
     */
    public enum ApplicantType {
        STUDENT("学生"),
        TEACHER("教师");

        private final String description;

        ApplicantType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 请假类型
     */
    public enum LeaveType {
        SICK("病假"),
        PERSONAL("事假"),
        OTHER("其他");

        private final String description;

        LeaveType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 审批状态
     */
    public enum ApprovalStatus {
        PENDING("待审批"),
        APPROVED("已批准"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        ApprovalStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 申请人类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false, length = 20)
    private ApplicantType applicantType;

    /**
     * 申请人ID（学生ID或教师ID）
     */
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    /**
     * 申请人学号/工号
     */
    @Column(name = "applicant_no", nullable = false, length = 20)
    private String applicantNo;

    /**
     * 请假类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    /**
     * 开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 请假天数
     */
    @Column(name = "days", nullable = false)
    private Integer days;

    /**
     * 请假原因
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 审批状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /**
     * 审批人ID
     */
    @Column(name = "approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Column(name = "approver_name", length = 50)
    private String approverName;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 附件URL（可选）
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
}

