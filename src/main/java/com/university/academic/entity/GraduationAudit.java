package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 毕业审核实体类
 * 记录学生的毕业资格审核结果
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "graduation_audit", indexes = {
    @Index(name = "idx_grad_audit_student", columnList = "student_id"),
    @Index(name = "idx_grad_audit_year", columnList = "audit_year"),
    @Index(name = "idx_grad_audit_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduationAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "audit_year", nullable = false)
    private Integer auditYear;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuditStatus status = AuditStatus.PENDING;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;

    @Column(name = "audited_by")
    private Long auditedBy;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    /**
     * 审核状态枚举
     */
    public enum AuditStatus {
        PENDING("待审核"),
        PASS("通过"),
        FAIL("不通过"),
        DEFERRED("暂缓毕业");

        private final String description;

        AuditStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

