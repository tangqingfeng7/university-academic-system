package com.university.academic.controller.dto;

import com.university.academic.entity.TeacherEvaluation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师评价DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEvaluationDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private Long courseOfferingId;
    private String courseNo;
    private String courseName;
    private String semesterName;
    private Integer teachingRating;
    private Integer attitudeRating;
    private Integer contentRating;
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
    public static TeacherEvaluationDTO fromEntity(TeacherEvaluation entity) {
        if (entity == null) {
            return null;
        }
        return TeacherEvaluationDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .studentName(entity.getAnonymous() ? "匿名" : entity.getStudent().getName())
                .teacherId(entity.getTeacher().getId())
                .teacherName(entity.getTeacher().getName())
                .courseOfferingId(entity.getCourseOffering().getId())
                .courseNo(entity.getCourseOffering().getCourse().getCourseNo())
                .courseName(entity.getCourseOffering().getCourse().getName())
                .semesterName(entity.getCourseOffering().getSemester().getSemesterName())
                .teachingRating(entity.getTeachingRating())
                .attitudeRating(entity.getAttitudeRating())
                .contentRating(entity.getContentRating())
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

