package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 奖学金申请实体类
 */
@Entity
@Table(name = "scholarship_application", indexes = {
    @Index(name = "idx_application_scholarship", columnList = "scholarship_id"),
    @Index(name = "idx_application_student", columnList = "student_id"),
    @Index(name = "idx_application_year", columnList = "academic_year"),
    @Index(name = "idx_application_status", columnList = "status")
})
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ScholarshipApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 奖学金
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id", nullable = false)
    private Scholarship scholarship;
    
    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    /**
     * 学年
     */
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;
    
    /**
     * GPA
     */
    @Column(nullable = false)
    private Double gpa;
    
    /**
     * 总学分
     */
    @Column(name = "total_credits", nullable = false)
    private Double totalCredits;
    
    /**
     * 综合得分
     */
    @Column(name = "comprehensive_score")
    private Double comprehensiveScore;
    
    /**
     * 个人陈述
     */
    @Column(name = "personal_statement", columnDefinition = "TEXT")
    private String personalStatement;
    
    /**
     * 证明材料URL
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    /**
     * 申请状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    /**
     * 提交时间
     */
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

