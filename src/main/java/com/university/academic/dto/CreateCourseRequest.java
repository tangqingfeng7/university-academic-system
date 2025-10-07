package com.university.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建课程请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    /**
     * 课程编号
     */
    @NotBlank(message = "课程编号不能为空")
    private String courseNo;

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    private String name;

    /**
     * 学分
     */
    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分必须大于0")
    private Integer credits;

    /**
     * 学时
     */
    @NotNull(message = "学时不能为空")
    @Min(value = 1, message = "学时必须大于0")
    private Integer hours;

    /**
     * 课程类型 (REQUIRED/ELECTIVE/PUBLIC)
     */
    @NotBlank(message = "课程类型不能为空")
    @Pattern(regexp = "REQUIRED|ELECTIVE|PUBLIC", message = "课程类型必须是REQUIRED、ELECTIVE或PUBLIC")
    private String type;

    /**
     * 院系ID
     */
    @NotNull(message = "院系ID不能为空")
    private Long departmentId;

    /**
     * 课程简介
     */
    private String description;
}

