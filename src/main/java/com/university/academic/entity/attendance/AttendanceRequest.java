package com.university.academic.entity.attendance;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 考勤申请实体类
 * 处理补签和考勤申诉请求
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_request", indexes = {
    @Index(name = "idx_student_status", columnList = "student_id, status"),
    @Index(name = "idx_detail", columnList = "detail_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest extends BaseEntity {

    /**
     * 申请类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private RequestType requestType;

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 考勤明细
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private AttendanceDetail attendanceDetail;

    /**
     * 申请原因
     */
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    /**
     * 附件URL
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * 申请状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

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
}

