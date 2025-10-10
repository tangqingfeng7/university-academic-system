package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户专属通知实体类
 * 用于发送给特定用户的通知
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "user_notification", indexes = {
    @Index(name = "idx_user_notification_user", columnList = "user_id"),
    @Index(name = "idx_user_notification_status", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification extends BaseEntity {

    /**
     * 接收用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 通知类型
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 关联业务ID（如申请ID）
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 关联业务类型（如STATUS_CHANGE）
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime;

    /**
     * 发送时间
     */
    @Column(name = "sent_time", nullable = false)
    @Builder.Default
    private LocalDateTime sentTime = LocalDateTime.now();
}

