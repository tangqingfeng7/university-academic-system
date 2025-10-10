-- =====================================================
-- 审批流程增强功能相关表
-- =====================================================

-- 用户专属通知表
CREATE TABLE user_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    reference_id BIGINT COMMENT '关联业务ID',
    reference_type VARCHAR(50) COMMENT '关联业务类型',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
    read_time TIMESTAMP COMMENT '阅读时间',
    sent_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_user_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_notification_user (user_id),
    INDEX idx_user_notification_status (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户专属通知表';

-- 审批配置表（审批时限配置）
CREATE TABLE approval_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    change_type VARCHAR(20) NOT NULL UNIQUE COMMENT '异动类型：SUSPENSION-休学, RESUMPTION-复学, TRANSFER-转专业, WITHDRAWAL-退学',
    deadline_days INT NOT NULL COMMENT '审批时限（天数）',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批配置表';

-- 审批级别审批人配置表（多审批人和负载均衡）
CREATE TABLE approval_level_approver (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    approval_level INT NOT NULL COMMENT '审批级别：1-辅导员, 2-院系, 3-教务处',
    user_id BIGINT NOT NULL COMMENT '审批人用户ID',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
    pending_count INT NOT NULL DEFAULT 0 COMMENT '当前待处理任务数（用于负载均衡）',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_approval_level_approver_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    UNIQUE KEY uk_level_user (approval_level, user_id),
    INDEX idx_level_approver_level (approval_level),
    INDEX idx_level_approver_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批级别审批人配置表';

-- 初始化审批配置数据（默认都是7天）
INSERT INTO approval_configuration (change_type, deadline_days, enabled, description) VALUES
('SUSPENSION', 7, 1, '休学审批时限'),
('RESUMPTION', 7, 1, '复学审批时限'),
('TRANSFER', 14, 1, '转专业审批时限（较长）'),
('WITHDRAWAL', 7, 1, '退学审批时限');

