package com.university.academic.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩录入请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeInputRequest {

    /**
     * 平时成绩
     */
    @DecimalMin(value = "0", message = "平时成绩不能小于0")
    @DecimalMax(value = "100", message = "平时成绩不能大于100")
    private BigDecimal regularScore;

    /**
     * 期中成绩
     */
    @DecimalMin(value = "0", message = "期中成绩不能小于0")
    @DecimalMax(value = "100", message = "期中成绩不能大于100")
    private BigDecimal midtermScore;

    /**
     * 期末成绩
     */
    @DecimalMin(value = "0", message = "期末成绩不能小于0")
    @DecimalMax(value = "100", message = "期末成绩不能大于100")
    private BigDecimal finalScore;
}

