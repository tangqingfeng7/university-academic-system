package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 学生学分汇总实体类
 * 记录学生已获得的各类学分统计
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "student_credit_summary", indexes = {
    @Index(name = "idx_credit_summary_student", columnList = "student_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCreditSummary extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "total_credits", nullable = false)
    @Builder.Default
    private Double totalCredits = 0.0;

    @Column(name = "required_credits", nullable = false)
    @Builder.Default
    private Double requiredCredits = 0.0;

    @Column(name = "elective_credits", nullable = false)
    @Builder.Default
    private Double electiveCredits = 0.0;

    @Column(name = "practical_credits", nullable = false)
    @Builder.Default
    private Double practicalCredits = 0.0;

    @Column(name = "gpa", nullable = false)
    @Builder.Default
    private Double gpa = 0.0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}

