package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {
    private Long id;
    private String classCode;
    private String className;
    private Long majorId;
    private String majorName;
    private String majorCode;
    private Long departmentId;
    private String departmentName;
    private Integer enrollmentYear;
    private Integer capacity;
    private Long counselorId;
    private String counselorName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

