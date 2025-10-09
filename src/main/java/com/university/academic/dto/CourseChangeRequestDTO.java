package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调课申请数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseChangeRequestDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请教师ID
     */
    private Long teacherId;

    /**
     * 申请教师姓名
     */
    private String teacherName;

    /**
     * 开课计划ID
     */
    private Long offeringId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 原时间安排（JSON格式）
     */
    private String originalSchedule;

    /**
     * 新时间安排（JSON格式）
     */
    private String newSchedule;

    /**
     * 调课原因
     */
    private String reason;

    /**
     * 状态
     */
    private String status;

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

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

