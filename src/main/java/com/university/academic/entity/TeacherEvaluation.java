package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 教师评价实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "teacher_evaluation",
       indexes = {
           @Index(name = "idx_teacher_eval_teacher", columnList = "teacher_id"),
           @Index(name = "idx_teacher_eval_offering", columnList = "course_offering_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherEvaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @Column(name = "teaching_rating", nullable = false)
    private Integer teachingRating;

    @Column(name = "attitude_rating", nullable = false)
    private Integer attitudeRating;

    @Column(name = "content_rating", nullable = false)
    private Integer contentRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean anonymous = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean flagged = false;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;
}

