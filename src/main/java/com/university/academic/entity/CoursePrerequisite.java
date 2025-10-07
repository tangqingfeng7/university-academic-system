package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 先修课程实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course_prerequisite",
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "prerequisite_course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePrerequisite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;
}

