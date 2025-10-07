package com.university.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新课程请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 学分
     */
    @Min(value = 1, message = "学分必须大于0")
    private Integer credits;

    /**
     * 学时
     */
    @Min(value = 1, message = "学时必须大于0")
    private Integer hours;

    /**
     * 课程类型 (REQUIRED/ELECTIVE/PUBLIC)
     */
    @Pattern(regexp = "REQUIRED|ELECTIVE|PUBLIC", message = "课程类型必须是REQUIRED、ELECTIVE或PUBLIC")
    private String type;

    /**
     * 院系ID
     */
    private Long departmentId;

    /**
     * 课程简介
     */
    private String description;
}

