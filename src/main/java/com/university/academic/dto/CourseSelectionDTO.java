package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课记录数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSelectionDTO {

    /**
     * 选课记录ID
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
     * 学分
     */
    private Integer credits;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 上课时间
     */
    private String schedule;

    /**
     * 上课地点
     */
    private String location;

    /**
     * 选课时间
     */
    private LocalDateTime selectionTime;

    /**
     * 状态 (SELECTED/DROPPED/COMPLETED)
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

