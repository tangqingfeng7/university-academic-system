package com.university.academic.controller.dto;

import com.university.academic.entity.UsageType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教室使用记录项DTO
 */
@Data
public class ClassroomUsageItemDTO {
    
    /**
     * 使用类型
     */
    private UsageType type;
    
    /**
     * 使用类型描述
     */
    private String typeDescription;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 标题/名称
     */
    private String title;
    
    /**
     * 描述/详情
     */
    private String description;
    
    /**
     * 关联ID（课程ID、考试ID、借用ID等）
     */
    private Long referenceId;
    
    /**
     * 教师姓名（课程或考试）
     */
    private String teacherName;
    
    /**
     * 课程名称（课程或考试）
     */
    private String courseName;
    
    /**
     * 申请人姓名（借用）
     */
    private String applicantName;
}

