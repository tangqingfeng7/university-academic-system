package com.university.academic.controller.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建教室借用请求
 */
@Data
public class CreateBookingRequest {
    
    /**
     * 教室ID
     */
    @NotNull(message = "教室ID不能为空")
    private Long classroomId;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    /**
     * 借用目的
     */
    @NotBlank(message = "借用目的不能为空")
    private String purpose;
    
    /**
     * 预计参加人数
     */
    @Min(value = 1, message = "参加人数必须大于0")
    private Integer expectedAttendees;
}

