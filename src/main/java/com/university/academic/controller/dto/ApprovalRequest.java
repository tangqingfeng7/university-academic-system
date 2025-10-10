package com.university.academic.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批请求
 */
@Data
public class ApprovalRequest {
    
    /**
     * 是否批准
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    /**
     * 审批意见
     */
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}

