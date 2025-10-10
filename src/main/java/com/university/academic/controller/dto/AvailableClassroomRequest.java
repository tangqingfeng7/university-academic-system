package com.university.academic.controller.dto;

import com.university.academic.entity.ClassroomType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询可用教室请求（用于排课系统）
 */
@Data
public class AvailableClassroomRequest {
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    /**
     * 最小容量（可选）
     */
    private Integer minCapacity;
    
    /**
     * 教室类型（可选）
     */
    private ClassroomType type;
    
    /**
     * 楼栋（可选）
     */
    private String building;
    
    /**
     * 是否只返回多媒体教室
     */
    private Boolean multimediaOnly;
}

