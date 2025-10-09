package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDTO {

    /**
     * 学期ID
     */
    private Long id;

    /**
     * 学年（如"2024-2025"）
     */
    private String academicYear;

    /**
     * 学期类型（1:春季 2:秋季）
     */
    private Integer semesterType;

    /**
     * 学期类型描述
     */
    private String semesterTypeName;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 带周次的学期名称
     */
    private String semesterNameWithWeek;

    /**
     * 当前周次
     */
    private Integer currentWeek;

    /**
     * 总周数
     */
    private Integer totalWeeks;

    /**
     * 学期开始日期
     */
    private LocalDate startDate;

    /**
     * 学期结束日期
     */
    private LocalDate endDate;

    /**
     * 选课开始时间
     */
    private LocalDateTime courseSelectionStart;

    /**
     * 选课结束时间
     */
    private LocalDateTime courseSelectionEnd;

    /**
     * 选课功能是否启用
     */
    private Boolean courseSelectionEnabled;

    /**
     * 选课状态（选课未开始/选课进行中/选课已结束/选课已关闭）
     */
    private String courseSelectionStatus;

    /**
     * 当前是否可以选课
     */
    private Boolean courseSelectionAvailable;

    /**
     * 是否为当前活动学期
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

