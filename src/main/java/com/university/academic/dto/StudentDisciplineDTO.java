package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生处分DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDisciplineDTO {

    private Long id;
    
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String majorName;
    private String className;
    
    private String disciplineType;
    private String disciplineTypeDescription;
    
    private String reason;
    private String description;
    
    private LocalDate occurrenceDate;
    private LocalDate punishmentDate;
    
    private String status;
    private String statusDescription;
    
    private String approvalStatus;
    private String approvalStatusDescription;
    
    private Boolean canRemove;
    
    private LocalDate removedDate;
    private String removedReason;
    private String removedByName;
    
    private String attachmentUrl;
    
    private String reporterName;
    private String approverName;
    private LocalDateTime approvedAt;
    private String approvalComment;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

