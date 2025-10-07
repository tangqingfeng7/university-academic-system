package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创建学期请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSemesterRequest {

    /**
     * 学年（如"2024-2025"）
     */
    @NotBlank(message = "学年不能为空")
    private String academicYear;

    /**
     * 学期类型（1:春季 2:秋季）
     */
    @NotNull(message = "学期类型不能为空")
    private Integer semesterType;

    /**
     * 学期开始日期
     */
    @NotNull(message = "学期开始日期不能为空")
    private LocalDate startDate;

    /**
     * 学期结束日期
     */
    @NotNull(message = "学期结束日期不能为空")
    private LocalDate endDate;

    /**
     * 选课开始时间
     */
    @NotNull(message = "选课开始时间不能为空")
    private LocalDateTime courseSelectionStart;

    /**
     * 选课结束时间
     */
    @NotNull(message = "选课结束时间不能为空")
    private LocalDateTime courseSelectionEnd;
}

