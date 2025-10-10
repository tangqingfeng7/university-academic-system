package com.university.academic.controller.dto;

import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import lombok.Data;

/**
 * 教室查询条件DTO
 */
@Data
public class ClassroomQueryDTO {
    
    /**
     * 楼栋
     */
    private String building;
    
    /**
     * 教室类型
     */
    private ClassroomType type;
    
    /**
     * 教室状态
     */
    private ClassroomStatus status;
    
    /**
     * 最小容量
     */
    private Integer minCapacity;
    
    /**
     * 教室编号（模糊查询）
     */
    private String roomNo;
}

