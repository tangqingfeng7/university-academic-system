package com.university.academic.controller.dto;

import com.university.academic.entity.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教室借用查询条件DTO
 */
@Data
public class BookingQueryDTO {
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 教室ID
     */
    private Long classroomId;
    
    /**
     * 借用状态
     */
    private BookingStatus status;
    
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    
    /**
     * 教室编号（模糊查询）
     */
    private String roomNo;
    
    /**
     * 申请人姓名（模糊查询）
     */
    private String applicantName;
}

