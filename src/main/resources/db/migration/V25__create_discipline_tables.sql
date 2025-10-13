-- =====================================================
-- 学生处分管理模块数据库表
-- =====================================================

-- 处分记录表
CREATE TABLE IF NOT EXISTS student_discipline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '处分ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    discipline_type VARCHAR(50) NOT NULL COMMENT '处分类型',
    reason TEXT NOT NULL COMMENT '处分原因',
    description TEXT COMMENT '详细描述',
    occurrence_date DATE NOT NULL COMMENT '违纪发生日期',
    punishment_date DATE NOT NULL COMMENT '处分日期',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '处分状态',
    can_remove BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否可解除',
    removed_date DATE COMMENT '解除日期',
    removed_reason TEXT COMMENT '解除原因',
    removed_by BIGINT COMMENT '解除操作人ID',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    reporter_id BIGINT COMMENT '上报人ID',
    approver_id BIGINT COMMENT '审批人ID',
    approved_at DATETIME COMMENT '审批时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    
    CONSTRAINT fk_discipline_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_discipline_reporter FOREIGN KEY (reporter_id) REFERENCES user(id),
    CONSTRAINT fk_discipline_approver FOREIGN KEY (approver_id) REFERENCES user(id),
    CONSTRAINT fk_discipline_removed_by FOREIGN KEY (removed_by) REFERENCES user(id),
    
    INDEX idx_student_id (student_id),
    INDEX idx_discipline_type (discipline_type),
    INDEX idx_status (status),
    INDEX idx_punishment_date (punishment_date),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生处分记录表';

-- 处分申诉表
CREATE TABLE IF NOT EXISTS discipline_appeal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申诉ID',
    discipline_id BIGINT NOT NULL COMMENT '处分ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    appeal_reason TEXT NOT NULL COMMENT '申诉理由',
    evidence TEXT COMMENT '申诉证据',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '申诉状态',
    review_result VARCHAR(50) COMMENT '审核结果',
    review_comment TEXT COMMENT '审核意见',
    reviewed_by BIGINT COMMENT '审核人ID',
    reviewed_at DATETIME COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_appeal_discipline FOREIGN KEY (discipline_id) REFERENCES student_discipline(id),
    CONSTRAINT fk_appeal_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_appeal_reviewer FOREIGN KEY (reviewed_by) REFERENCES user(id),
    
    INDEX idx_discipline_id (discipline_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处分申诉表';

-- 添加注释
ALTER TABLE student_discipline COMMENT = '学生处分记录表：记录学生违纪处分信息';
ALTER TABLE discipline_appeal COMMENT = '处分申诉表：记录学生对处分的申诉';

