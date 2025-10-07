package com.university.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建开课计划请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseOfferingRequest {

    /**
     * 学期ID
     */
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 上课时间（JSON格式）
     * 例如: [{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]
     */
    @NotBlank(message = "上课时间不能为空")
    private String schedule;

    /**
     * 上课地点
     */
    @NotBlank(message = "上课地点不能为空")
    private String location;

    /**
     * 容量
     */
    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;
}

