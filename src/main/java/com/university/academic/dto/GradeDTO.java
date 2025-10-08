package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {

    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 选课记录ID
     */
    private Long courseSelectionId;

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
     * 学分
     */
    private Integer credits;

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 平时成绩
     */
    private BigDecimal regularScore;

    /**
     * 期中成绩
     */
    private BigDecimal midtermScore;

    /**
     * 期末成绩
     */
    private BigDecimal finalScore;

    /**
     * 总评成绩
     */
    private BigDecimal totalScore;

    /**
     * 绩点
     */
    private BigDecimal gradePoint;

    /**
     * 是否及格
     */
    private Boolean passed;

    /**
     * 状态 (DRAFT/SUBMITTED/PUBLISHED)
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

