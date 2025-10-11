package com.university.academic.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建学费标准请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTuitionStandardRequest {

    /**
     * 专业ID
     */
    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    /**
     * 学年
     */
    @NotBlank(message = "学年不能为空")
    @Pattern(regexp = "\\d{4}-\\d{4}", message = "学年格式不正确，应为：2024-2025")
    private String academicYear;

    /**
     * 年级
     */
    @NotNull(message = "年级不能为空")
    @Min(value = 1, message = "年级必须大于0")
    @Max(value = 4, message = "年级不能超过4")
    private Integer gradeLevel;

    /**
     * 学费
     */
    @NotNull(message = "学费不能为空")
    @DecimalMin(value = "0.0", message = "学费不能为负数")
    private Double tuitionFee;

    /**
     * 住宿费
     */
    @NotNull(message = "住宿费不能为空")
    @DecimalMin(value = "0.0", message = "住宿费不能为负数")
    private Double accommodationFee;

    /**
     * 教材费
     */
    @NotNull(message = "教材费不能为空")
    @DecimalMin(value = "0.0", message = "教材费不能为负数")
    private Double textbookFee;

    /**
     * 其他费用
     */
    @DecimalMin(value = "0.0", message = "其他费用不能为负数")
    private Double otherFees;
}

