package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 课程评价实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course_evaluation",
       indexes = {
           @Index(name = "idx_course_eval_student", columnList = "student_id"),
           @Index(name = "idx_course_eval_offering", columnList = "course_offering_id"),
           @Index(name = "idx_course_eval_semester", columnList = "semester_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEvaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean anonymous = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean flagged = false;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;
}

