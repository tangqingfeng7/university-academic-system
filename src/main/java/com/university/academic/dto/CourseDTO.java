package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 学分
     */
    private Integer credits;

    /**
     * 学时
     */
    private Integer hours;

    /**
     * 课程类型 (REQUIRED/ELECTIVE/PUBLIC)
     */
    private String type;

    /**
     * 课程类型描述
     */
    private String typeDescription;

    /**
     * 院系ID
     */
    private Long departmentId;

    /**
     * 院系名称
     */
    private String departmentName;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 先修课程列表
     */
    private List<CourseDTO> prerequisites;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

