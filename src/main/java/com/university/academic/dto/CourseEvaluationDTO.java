package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程评价数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEvaluationDTO {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 开课计划ID
     */
    private Long offeringId;

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
     * 教师姓名
     */
    private String teacherName;

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 评论内容
     */
    private String comment;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 是否被标记
     */
    private Boolean flagged;

    /**
     * 审核备注
     */
    private String moderationNote;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

