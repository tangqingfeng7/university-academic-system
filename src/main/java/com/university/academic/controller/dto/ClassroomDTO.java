package com.university.academic.controller.dto;

import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教室DTO
 */
@Data
public class ClassroomDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 教室编号
     */
    private String roomNo;
    
    /**
     * 所在楼栋
     */
    private String building;
    
    /**
     * 容量（人数）
     */
    private Integer capacity;
    
    /**
     * 教室类型
     */
    private ClassroomType type;
    
    /**
     * 教室类型描述
     */
    private String typeDescription;
    
    /**
     * 设备信息（JSON格式）
     */
    private String equipment;
    
    /**
     * 教室状态
     */
    private ClassroomStatus status;
    
    /**
     * 教室状态描述
     */
    private String statusDescription;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

