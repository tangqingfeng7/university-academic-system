package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 通知公告实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;

    @Column(name = "target_role", length = 50)
    private String targetRole;

    @Column(name = "publish_time", nullable = false)
    @Builder.Default
    private LocalDateTime publishTime = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        SYSTEM("系统通知"),
        COURSE("课程通知"),
        GRADE("成绩通知"),
        SELECTION("选课通知"),
        EXAM("考试通知"),
        SCHOLARSHIP_APPROVAL("奖学金审批通知"),
        SCHOLARSHIP_APPROVAL_PENDING("待审批奖学金通知"),
        SCHEDULE("排课通知"),
        ATTENDANCE("考勤通知"),
        ATTENDANCE_WARNING("考勤预警"),
        ATTENDANCE_APPEAL("考勤申请");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

