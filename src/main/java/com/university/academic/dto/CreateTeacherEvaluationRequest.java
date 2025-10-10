package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 创建教师评价请求DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeacherEvaluationRequest {

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 开课计划ID
     */
    @NotNull(message = "开课计划ID不能为空")
    private Long offeringId;

    /**
     * 教学能力评分（1-5）
     */
    @NotNull(message = "教学能力评分不能为空")
    @Min(value = 1, message = "教学能力评分不能低于1")
    @Max(value = 5, message = "教学能力评分不能高于5")
    private Integer teachingRating;

    /**
     * 教学态度评分（1-5）
     */
    @NotNull(message = "教学态度评分不能为空")
    @Min(value = 1, message = "教学态度评分不能低于1")
    @Max(value = 5, message = "教学态度评分不能高于5")
    private Integer attitudeRating;

    /**
     * 教学内容评分（1-5）
     */
    @NotNull(message = "教学内容评分不能为空")
    @Min(value = 1, message = "教学内容评分不能低于1")
    @Max(value = 5, message = "教学内容评分不能高于5")
    private Integer contentRating;

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

