package com.university.academic.vo.attendance;

import com.university.academic.entity.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤明细视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDetailVO {

    /**
     * 明细ID
     */
    private Long id;

    /**
     * 考勤记录ID
     */
    private Long recordId;

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
     * 班级
     */
    private String className;

    /**
     * 考勤状态
     */
    private AttendanceStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 签到时间
     */
    private LocalDateTime checkinTime;

    /**
     * 是否补签
     */
    private Boolean isMakeup;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 修改人ID
     */
    private Long modifiedBy;

    /**
     * 修改原因
     */
    private String modifyReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

