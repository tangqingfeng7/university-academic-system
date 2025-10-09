package com.university.academic.dto;

import com.university.academic.entity.ExamType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建考试请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    /**
     * 考试名称
     */
    @NotBlank(message = "考试名称不能为空")
    private String name;

    /**
     * 考试类型
     */
    @NotNull(message = "考试类型不能为空")
    private ExamType type;

    /**
     * 开课计划ID
     */
    @NotNull(message = "开课计划ID不能为空")
    private Long courseOfferingId;

    /**
     * 考试时间
     */
    @NotNull(message = "考试时间不能为空")
    @Future(message = "考试时间必须晚于当前时间")
    private LocalDateTime examTime;

    /**
     * 考试时长（分钟）
     */
    @NotNull(message = "考试时长不能为空")
    @Min(value = 30, message = "考试时长不能少于30分钟")
    @Max(value = 300, message = "考试时长不能超过300分钟")
    private Integer duration;

    /**
     * 总分
     */
    @NotNull(message = "总分不能为空")
    @Min(value = 1, message = "总分必须大于0")
    @Builder.Default
    private Integer totalScore = 100;

    /**
     * 考试说明
     */
    private String description;
}

