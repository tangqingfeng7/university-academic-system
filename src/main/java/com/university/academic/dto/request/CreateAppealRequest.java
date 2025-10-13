package com.university.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建申诉请求
 *
 * @author Academic System Team
 */
@Data
public class CreateAppealRequest {

    @NotNull(message = "处分ID不能为空")
    private Long disciplineId;

    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;

    private String evidence;

    private String attachmentUrl;
}

