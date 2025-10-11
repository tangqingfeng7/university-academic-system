package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 毕业要求实体类
 * 记录各专业各入学年份的毕业学分要求
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "graduation_requirement", 
    indexes = {
        @Index(name = "idx_grad_req_major", columnList = "major_id"),
        @Index(name = "idx_grad_req_year", columnList = "enrollment_year")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_major_enrollment_year", 
                         columnNames = {"major_id", "enrollment_year"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduationRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    @Column(name = "total_credits_required", nullable = false)
    private Double totalCreditsRequired;

    @Column(name = "required_credits_required", nullable = false)
    private Double requiredCreditsRequired;

    @Column(name = "elective_credits_required", nullable = false)
    private Double electiveCreditsRequired;

    @Column(name = "practical_credits_required", nullable = false)
    private Double practicalCreditsRequired;

    @Column(name = "additional_requirements", columnDefinition = "TEXT")
    private String additionalRequirements;
}

