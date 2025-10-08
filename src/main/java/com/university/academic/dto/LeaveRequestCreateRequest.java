package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 请假申请创建请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestCreateRequest {

    /**
     * 请假类型：SICK病假, PERSONAL事假, OTHER其他
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 请假原因
     */
    @NotBlank(message = "请假原因不能为空")
    private String reason;

    /**
     * 附件URL
     */
    private String attachmentUrl;
}

