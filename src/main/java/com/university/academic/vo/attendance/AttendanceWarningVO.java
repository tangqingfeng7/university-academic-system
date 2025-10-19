package com.university.academic.vo.attendance;

import com.university.academic.entity.attendance.WarningStatus;
import com.university.academic.entity.attendance.WarningType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤预警视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceWarningVO {

    /**
     * 预警ID
     */
    private Long id;

    /**
     * 预警类型
     */
    private WarningType warningType;

    /**
     * 预警类型描述
     */
    private String warningTypeDesc;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 开课计划ID
     */
    private Long offeringId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 预警级别（1-3）
     */
    private Integer warningLevel;

    /**
     * 预警消息
     */
    private String warningMessage;

    /**
     * 预警数据
     */
    private String warningData;

    /**
     * 处理状态
     */
    private WarningStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 处理人ID
     */
    private Long handledBy;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 处理意见
     */
    private String handleComment;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

