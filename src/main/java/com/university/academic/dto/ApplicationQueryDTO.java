package com.university.academic.dto;

import com.university.academic.entity.ApplicationStatus;
import lombok.Data;

/**
 * 奖学金申请查询DTO
 */
@Data
public class ApplicationQueryDTO {
    
    /**
     * 奖学金ID
     */
    private Long scholarshipId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 申请状态
     */
    private ApplicationStatus status;
    
    /**
     * 院系ID
     */
    private Long departmentId;
    
    /**
     * 专业ID
     */
    private Long majorId;
}

