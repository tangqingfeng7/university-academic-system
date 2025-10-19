package com.university.academic.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申诉/补签请求DTO
 *
 * @author Academic System Team
 */
@Data
public class AppealRequestDTO {

    /**
     * 考勤明细ID
     */
    @NotNull(message = "考勤明细ID不能为空")
    private Long detailId;

    /**
     * 申请原因
     */
    @NotBlank(message = "申请原因不能为空")
    private String reason;

    /**
     * 附件URL（可选）
     */
    private String attachmentUrl;
}

