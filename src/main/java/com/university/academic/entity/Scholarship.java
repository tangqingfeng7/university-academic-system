package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 奖学金类型实体类
 */
@Entity
@Table(name = "scholarship")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Scholarship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 奖学金名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 奖学金等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScholarshipLevel level;
    
    /**
     * 奖学金金额
     */
    @Column(nullable = false)
    private Double amount;
    
    /**
     * 名额
     */
    @Column(nullable = false)
    private Integer quota;
    
    /**
     * 描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 最低GPA要求
     */
    @Column(name = "min_gpa")
    private Double minGpa;
    
    /**
     * 最低学分要求
     */
    @Column(name = "min_credits")
    private Double minCredits;
    
    /**
     * 其他要求（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean active = true;
    
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

