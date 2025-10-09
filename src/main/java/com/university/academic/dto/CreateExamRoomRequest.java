package com.university.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建考场请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRoomRequest {

    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /**
     * 考场名称
     */
    @NotBlank(message = "考场名称不能为空")
    private String roomName;

    /**
     * 考场地点
     */
    @NotBlank(message = "考场地点不能为空")
    private String location;

    /**
     * 考场容量
     */
    @NotNull(message = "考场容量不能为空")
    @Min(value = 1, message = "考场容量必须大于0")
    private Integer capacity;
}

