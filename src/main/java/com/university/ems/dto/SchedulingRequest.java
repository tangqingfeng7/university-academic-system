package com.university.ems.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 排课请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class SchedulingRequest {

    /**
     * 学期ID
     */
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    /**
     * 方案名称
     */
    private String solutionName;

    /**
     * 指定的课程ID列表（可选，如果为空则为该学期所有课程排课）
     */
    private List<Long> courseOfferingIds;

    /**
     * 是否考虑教师偏好（默认true）
     */
    private Boolean considerTeacherPreference = true;

    /**
     * 最大迭代次数（默认1000）
     */
    private Integer maxIterations = 1000;

    /**
     * 超时时间（秒，默认300秒）
     */
    private Integer timeoutSeconds = 300;
}

