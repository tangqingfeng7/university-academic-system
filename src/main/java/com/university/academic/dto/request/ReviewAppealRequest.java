package com.university.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核申诉请求
 *
 * @author Academic System Team
 */
@Data
public class ReviewAppealRequest {

    @NotBlank(message = "审核结果不能为空")
    private String result;

    private String comment;
}

