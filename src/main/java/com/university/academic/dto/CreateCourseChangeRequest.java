package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建调课申请请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseChangeRequest {

    /**
     * 开课计划ID
     */
    @NotNull(message = "开课计划ID不能为空")
    private Long offeringId;

    /**
     * 新时间安排（JSON格式）
     */
    @NotBlank(message = "新时间安排不能为空")
    private String newSchedule;

    /**
     * 调课原因
     */
    @NotBlank(message = "调课原因不能为空")
    private String reason;
}

