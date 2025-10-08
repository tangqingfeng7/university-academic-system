package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请假审批请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestApprovalRequest {

    /**
     * 审批状态：APPROVED已批准, REJECTED已拒绝
     */
    @NotBlank(message = "审批状态不能为空")
    private String status;

    /**
     * 审批意见
     */
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}

