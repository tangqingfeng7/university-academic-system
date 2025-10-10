package com.university.academic.dto;

import com.university.academic.entity.ApprovalAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批请求DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    /**
     * 审批操作
     */
    @NotNull(message = "审批操作不能为空")
    private ApprovalAction action;

    /**
     * 审批意见
     */
    private String comment;
}

