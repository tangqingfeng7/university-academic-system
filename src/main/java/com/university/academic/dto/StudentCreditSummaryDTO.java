package com.university.academic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生学分汇总DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生学分汇总信息")
public class StudentCreditSummaryDTO {

    @Schema(description = "汇总ID")
    private Long id;

    @Schema(description = "学生ID", required = true)
    private Long studentId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "入学年份", example = "2020")
    private Integer enrollmentYear;

    @Schema(description = "总学分", example = "120.0")
    private Double totalCredits;

    @Schema(description = "必修学分", example = "80.0")
    private Double requiredCredits;

    @Schema(description = "选修学分", example = "30.0")
    private Double electiveCredits;

    @Schema(description = "实践学分", example = "10.0")
    private Double practicalCredits;

    @Schema(description = "平均绩点GPA（0.0-4.0）", example = "3.5")
    private Double gpa;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

