package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师考试数据传输对象（用于教师端展示）
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherExamDTO {

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
     * 考试状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 考场总数
     */
    private Integer totalRooms;

    /**
     * 学生总数
     */
    private Integer totalStudents;

    /**
     * 考试说明
     */
    private String description;
}

