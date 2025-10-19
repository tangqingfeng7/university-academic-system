package com.university.academic.vo.attendance;

import com.university.academic.entity.attendance.RequestStatus;
import com.university.academic.entity.attendance.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤申请视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestVO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请类型
     */
    private RequestType requestType;

    /**
     * 申请类型描述
     */
    private String requestTypeDesc;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 考勤明细ID
     */
    private Long detailId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 原考勤状态
     */
    private String originalStatus;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 申请状态
     */
    private RequestStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

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
     * 创建时间
     */
    private LocalDateTime createdAt;
}

