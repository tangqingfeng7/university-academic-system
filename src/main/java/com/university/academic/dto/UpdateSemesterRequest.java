package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 更新学期请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSemesterRequest {

    /**
     * 学年（如"2024-2025"）
     */
    private String academicYear;

    /**
     * 学期类型（1:春季 2:秋季）
     */
    private Integer semesterType;

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
}

