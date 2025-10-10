package com.university.academic.controller.dto;

import com.university.academic.entity.ClassroomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建教室请求
 */
@Data
public class CreateClassroomRequest {
    
    /**
     * 教室编号
     */
    @NotBlank(message = "教室编号不能为空")
    private String roomNo;
    
    /**
     * 所在楼栋
     */
    @NotBlank(message = "楼栋不能为空")
    private String building;
    
    /**
     * 容量（人数）
     */
    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;
    
    /**
     * 教室类型
     */
    @NotNull(message = "教室类型不能为空")
    private ClassroomType type;
    
    /**
     * 设备信息（JSON格式）
     */
    private String equipment;
}

