package com.university.academic.controller.dto;

import com.university.academic.entity.CourseOffering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可评价课程DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCourseDTO {
    private Long offeringId;
    private String courseNo;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String semesterName;

    /**
     * 从Entity转换为DTO
     */
    public static AvailableCourseDTO fromEntity(CourseOffering offering) {
        if (offering == null) {
            return null;
        }
        return AvailableCourseDTO.builder()
                .offeringId(offering.getId())
                .courseNo(offering.getCourse().getCourseNo())
                .courseName(offering.getCourse().getName())
                .teacherId(offering.getTeacher().getId())
                .teacherName(offering.getTeacher().getName())
                .semesterName(offering.getSemester().getSemesterName())
                .build();
    }
}

