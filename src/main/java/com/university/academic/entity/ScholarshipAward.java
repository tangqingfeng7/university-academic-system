package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 奖学金获奖记录实体类
 */
@Entity
@Table(name = "scholarship_award", indexes = {
    @Index(name = "idx_award_student", columnList = "student_id"),
    @Index(name = "idx_award_year", columnList = "academic_year"),
    @Index(name = "idx_award_scholarship", columnList = "scholarship_id")
})
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ScholarshipAward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 申请记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ScholarshipApplication application;
    
    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    /**
     * 奖学金
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id", nullable = false)
    private Scholarship scholarship;
    
    /**
     * 学年
     */
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;
    
    /**
     * 奖学金金额
     */
    @Column(nullable = false)
    private Double amount;
    
    /**
     * 获奖时间
     */
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
    
    /**
     * 是否已公示
     */
    @Column(nullable = false)
    private Boolean published = false;
    
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

