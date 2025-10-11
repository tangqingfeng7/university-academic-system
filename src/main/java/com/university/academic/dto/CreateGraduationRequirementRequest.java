package com.university.academic.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建毕业要求请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGraduationRequirementRequest {

    /**
     * 专业ID
     */
    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    /**
     * 入学年份
     */
    @NotNull(message = "入学年份不能为空")
    @Min(value = 2000, message = "入学年份不能小于2000")
    @Max(value = 2100, message = "入学年份不能大于2100")
    private Integer enrollmentYear;

    /**
     * 总学分要求
     */
    @NotNull(message = "总学分要求不能为空")
    @DecimalMin(value = "0.0", message = "总学分要求不能为负数")
    private Double totalCreditsRequired;

    /**
     * 必修学分要求
     */
    @NotNull(message = "必修学分要求不能为空")
    @DecimalMin(value = "0.0", message = "必修学分要求不能为负数")
    private Double requiredCreditsRequired;

    /**
     * 选修学分要求
     */
    @NotNull(message = "选修学分要求不能为空")
    @DecimalMin(value = "0.0", message = "选修学分要求不能为负数")
    private Double electiveCreditsRequired;

    /**
     * 实践学分要求
     */
    @NotNull(message = "实践学分要求不能为空")
    @DecimalMin(value = "0.0", message = "实践学分要求不能为负数")
    private Double practicalCreditsRequired;

    /**
     * 其他要求（JSON格式字符串）
     */
    private String additionalRequirements;
}

