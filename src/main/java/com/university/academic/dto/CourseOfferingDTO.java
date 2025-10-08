package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 开课计划数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingDTO {

    /**
     * 开课计划ID
     */
    private Long id;

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
     * 课程类型 (REQUIRED/ELECTIVE/PUBLIC)
     */
    private String courseType;

    /**
     * 学分
     */
    private Integer credits;

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
     * 上课时间（JSON格式）
     */
    private String schedule;

    /**
     * 上课地点
     */
    private String location;

    /**
     * 容量
     */
    private Integer capacity;

    /**
     * 已选人数
     */
    private Integer enrolled;

    /**
     * 剩余名额
     */
    private Integer remainingCapacity;

    /**
     * 状态 (DRAFT/PUBLISHED/CANCELLED)
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

