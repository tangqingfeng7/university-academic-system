package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "operation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(length = 200)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(length = 50)
    private String ip;

    @Column(name = "execution_time")
    private Long executionTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LogStatus status;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 日志状态枚举
     */
    public enum LogStatus {
        SUCCESS("成功"),
        FAILURE("失败");

        private final String description;

        LogStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

