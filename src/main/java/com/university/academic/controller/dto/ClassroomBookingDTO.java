package com.university.academic.controller.dto;

import com.university.academic.entity.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教室借用DTO
 */
@Data
public class ClassroomBookingDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 教室信息
     */
    private ClassroomDTO classroom;
    
    /**
     * 教室ID
     */
    private Long classroomId;
    
    /**
     * 教室编号
     */
    private String classroomNo;
    
    /**
     * 教室名称（楼栋+编号）
     */
    private String classroomName;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 借用目的
     */
    private String purpose;
    
    /**
     * 预计参加人数
     */
    private Integer expectedAttendees;
    
    /**
     * 借用状态
     */
    private BookingStatus status;
    
    /**
     * 借用状态描述
     */
    private String statusDescription;
    
    /**
     * 审批人ID
     */
    private Long approvedBy;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批意见
     */
    private String approvalComment;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

