package com.university.academic.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新开课计划请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseOfferingRequest {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 上课时间（JSON格式）
     */
    private String schedule;

    /**
     * 上课地点
     */
    private String location;

    /**
     * 容量
     */
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;
}

