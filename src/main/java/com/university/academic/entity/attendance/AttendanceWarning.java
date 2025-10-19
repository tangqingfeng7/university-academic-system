package com.university.academic.entity.attendance;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.CourseOffering;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 考勤预警实体类
 * 记录考勤异常预警信息
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_warning", indexes = {
    @Index(name = "idx_target", columnList = "target_type, target_id"),
    @Index(name = "idx_status_level", columnList = "status, warning_level"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceWarning extends BaseEntity {

    /**
     * 预警类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "warning_type", nullable = false, length = 30)
    private WarningType warningType;

    /**
     * 目标类型（STUDENT/COURSE/TEACHER）
     */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    /**
     * 目标ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 目标名称
     */
    @Column(name = "target_name", length = 100)
    private String targetName;

    /**
     * 相关课程（可选）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id")
    private CourseOffering offering;

    /**
     * 预警级别（1-3）
     * 1: 轻度  2: 中度  3: 严重
     */
    @Column(name = "warning_level", nullable = false)
    private Integer warningLevel;

    /**
     * 预警消息
     */
    @Column(name = "warning_message", columnDefinition = "TEXT")
    private String warningMessage;

    /**
     * 预警数据（JSON格式）
     */
    @Column(name = "warning_data", columnDefinition = "JSON")
    private String warningData;

    /**
     * 处理状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WarningStatus status = WarningStatus.PENDING;

    /**
     * 处理人ID
     */
    @Column(name = "handled_by")
    private Long handledBy;

    /**
     * 处理时间
     */
    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    /**
     * 处理意见
     */
    @Column(name = "handle_comment", columnDefinition = "TEXT")
    private String handleComment;
}

