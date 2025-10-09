package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考试统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatisticsDTO {
    
    /**
     * 学期ID
     */
    private Long semesterId;
    
    /**
     * 学期名称
     */
    private String semesterName;
    
    /**
     * 考试总数
     */
    private Long totalExams;
    
    /**
     * 草稿数
     */
    private Long draftExams;
    
    /**
     * 已发布数
     */
    private Long publishedExams;
    
    /**
     * 进行中数
     */
    private Long inProgressExams;
    
    /**
     * 已结束数
     */
    private Long finishedExams;
    
    /**
     * 已取消数
     */
    private Long cancelledExams;
    
    /**
     * 考场总数
     */
    private Long totalRooms;
    
    /**
     * 学生总数
     */
    private Long totalStudents;
}

