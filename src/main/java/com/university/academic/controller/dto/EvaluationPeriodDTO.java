package com.university.academic.controller.dto;

import com.university.academic.entity.EvaluationPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价周期DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationPeriodDTO {
    private Long id;
    private Long semesterId;
    private String semesterName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从Entity转换为DTO
     */
    public static EvaluationPeriodDTO fromEntity(EvaluationPeriod entity) {
        if (entity == null) {
            return null;
        }
        return EvaluationPeriodDTO.builder()
                .id(entity.getId())
                .semesterId(entity.getSemester().getId())
                .semesterName(entity.getSemester().getSemesterName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .description(entity.getDescription())
                .isActive(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

