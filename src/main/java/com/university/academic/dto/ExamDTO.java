package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考试数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {

    /**
     * 考试ID
     */
    private Long id;

    /**
     * 考试名称
     */
    private String name;

    /**
     * 考试类型
     */
    private String type;

    /**
     * 考试类型描述
     */
    private String typeDescription;

    /**
     * 开课计划ID
     */
    private Long courseOfferingId;

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 考试时间
     */
    private LocalDateTime examTime;

    /**
     * 考试时长（分钟）
     */
    private Integer duration;

    /**
     * 总分
     */
    private Integer totalScore;

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

    /**
     * 考场总数
     */
    private Integer totalRooms;

    /**
     * 学生总数
     */
    private Integer totalStudents;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

