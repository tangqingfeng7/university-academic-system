package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 审批配置实体类
 * 配置不同异动类型的审批时限和审批人
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "approval_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalConfiguration extends BaseEntity {

    /**
     * 异动类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private ChangeType changeType;

    /**
     * 审批时限（天数）
     */
    @Column(name = "deadline_days", nullable = false)
    private Integer deadlineDays;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}

