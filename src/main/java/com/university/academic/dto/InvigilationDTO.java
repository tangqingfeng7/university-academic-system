package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监考任务数据传输对象（用于教师端展示）
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvigilationDTO {

    /**
     * 监考安排ID
     */
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 考试类型
     */
    private String examType;

    /**
     * 考试类型描述
     */
    private String examTypeDescription;

    /**
     * 考试时间
     */
    private LocalDateTime examTime;

    /**
     * 考试时长（分钟）
     */
    private Integer duration;

    /**
     * 考场ID
     */
    private Long examRoomId;

    /**
     * 考场名称
     */
    private String roomName;

    /**
     * 考场地点
     */
    private String location;

    /**
     * 监考类型
     */
    private String invigilatorType;

    /**
     * 监考类型描述
     */
    private String invigilatorTypeDescription;

    /**
     * 考试状态
     */
    private String examStatus;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 考场学生人数
     */
    private Integer studentCount;

    /**
     * 考场容量
     */
    private Integer roomCapacity;
}

