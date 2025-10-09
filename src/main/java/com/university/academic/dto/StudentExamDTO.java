package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生考试数据传输对象（用于学生端展示）
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamDTO {

    /**
     * 考试ID
     */
    private Long id;

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
    private String type;

    /**
     * 考试类型描述
     */
    private String typeDescription;

    /**
     * 考试时间
     */
    private LocalDateTime examTime;

    /**
     * 考试时长（分钟）
     */
    private Integer duration;

    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;

    /**
     * 考场名称
     */
    private String roomName;

    /**
     * 考场地点
     */
    private String location;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 考试状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 考试说明
     */
    private String description;
}

