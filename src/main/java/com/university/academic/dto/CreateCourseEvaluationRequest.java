package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 创建课程评价请求DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseEvaluationRequest {

    /**
     * 开课计划ID
     */
    @NotNull(message = "开课计划ID不能为空")
    private Long offeringId;

    /**
     * 评分（1-5星）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能低于1")
    @Max(value = 5, message = "评分不能高于5")
    private Integer rating;

    /**
     * 评论内容
     */
    @Size(max = 1000, message = "评论内容不能超过1000字")
    private String comment;

    /**
     * 是否匿名
     */
    private Boolean anonymous;
}

