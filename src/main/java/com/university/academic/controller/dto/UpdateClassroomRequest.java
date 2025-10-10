package com.university.academic.controller.dto;

import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 更新教室请求
 */
@Data
public class UpdateClassroomRequest {
    
    /**
     * 所在楼栋
     */
    private String building;
    
    /**
     * 容量（人数）
     */
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;
    
    /**
     * 教室类型
     */
    private ClassroomType type;
    
    /**
     * 设备信息（JSON格式）
     */
    private String equipment;
    
    /**
     * 教室状态
     */
    private ClassroomStatus status;
}

