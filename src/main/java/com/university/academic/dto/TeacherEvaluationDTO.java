package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师评价数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEvaluationDTO {

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
     * 教学能力评分
     */
    private Integer teachingRating;

    /**
     * 教学态度评分
     */
    private Integer attitudeRating;

    /**
     * 教学内容评分
     */
    private Integer contentRating;

    /**
     * 综合评分
     */
    private Double overallRating;

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

