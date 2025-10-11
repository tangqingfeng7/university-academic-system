package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 奖学金审批记录实体类
 */
@Entity
@Table(name = "scholarship_approval", indexes = {
    @Index(name = "idx_approval_application", columnList = "application_id"),
    @Index(name = "idx_approval_level", columnList = "approval_level")
})
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ScholarshipApproval {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 奖学金申请
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ScholarshipApplication application;
    
    /**
     * 审批级别
     * 1-辅导员
     * 2-院系
     */
    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;
    
    /**
     * 审批人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;
    
    /**
     * 审批操作
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction action;
    
    /**
     * 审批意见
     */
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    /**
     * 审批时间
     */
    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;
    
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

