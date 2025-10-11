package com.university.academic.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新毕业要求请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGraduationRequirementRequest {

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

