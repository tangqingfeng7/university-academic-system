package com.university.academic.controller.dto;

import com.university.academic.entity.CourseEvaluation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程评价DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEvaluationDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseOfferingId;
    private String courseNo;
    private String courseName;
    private String teacherName;
    private String semesterName;
    private Integer rating;
    private String comment;
    private Boolean anonymous;
    private String status;
    private Boolean flagged;
    private String moderationNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从Entity转换为DTO
     */
    public static CourseEvaluationDTO fromEntity(CourseEvaluation entity) {
        if (entity == null) {
            return null;
        }
        return CourseEvaluationDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .studentName(entity.getAnonymous() ? "匿名" : entity.getStudent().getName())
                .courseOfferingId(entity.getCourseOffering().getId())
                .courseNo(entity.getCourseOffering().getCourse().getCourseNo())
                .courseName(entity.getCourseOffering().getCourse().getName())
                .teacherName(entity.getCourseOffering().getTeacher().getName())
                .semesterName(entity.getCourseOffering().getSemester().getSemesterName())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .anonymous(entity.getAnonymous())
                .status(entity.getStatus().name())
                .flagged(entity.getFlagged())
                .moderationNote(entity.getModerationNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

