package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批调课申请请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveCourseChangeRequest {

    /**
     * 是否通过
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /**
     * 审批意见
     */
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}

