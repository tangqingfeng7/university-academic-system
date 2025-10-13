package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处分申诉DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineAppealDTO {

    private Long id;
    
    private Long disciplineId;
    private String disciplineTypeDescription;
    private String disciplineReason;
    
    private Long studentId;
    private String studentNo;
    private String studentName;
    
    private String appealReason;
    private String evidence;
    private String attachmentUrl;
    
    private String status;
    private String statusDescription;
    
    private String reviewResult;
    private String reviewResultDescription;
    private String reviewComment;
    
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

