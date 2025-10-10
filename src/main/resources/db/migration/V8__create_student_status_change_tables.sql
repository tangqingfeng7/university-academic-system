-- =====================================================
-- 学籍异动管理模块数据表
-- =====================================================

-- 学籍异动表
CREATE TABLE student_status_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    type VARCHAR(20) NOT NULL COMMENT '异动类型：SUSPENSION-休学, RESUMPTION-复学, TRANSFER-转专业, WITHDRAWAL-退学',
    reason TEXT NOT NULL COMMENT '异动原因',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    target_major_id BIGINT COMMENT '目标专业ID（转专业时使用）',
    attachment_url VARCHAR(500) COMMENT '证明材料附件URL',
    status VARCHAR(20) NOT NULL COMMENT '审批状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    approval_level INT NOT NULL COMMENT '当前审批级别：1-辅导员, 2-院系, 3-教务处',
    deadline TIMESTAMP COMMENT '审批截止时间',
    is_overdue TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已超时',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_status_change_student FOREIGN KEY (student_id) REFERENCES student(id),
    INDEX idx_status_change_student (student_id),
    INDEX idx_status_change_type (type),
    INDEX idx_status_change_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍异动表';

-- 学籍异动审批记录表
CREATE TABLE status_change_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    status_change_id BIGINT NOT NULL COMMENT '异动申请ID',
    approval_level INT NOT NULL COMMENT '审批级别：1-辅导员, 2-院系, 3-教务处',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    action VARCHAR(20) NOT NULL COMMENT '审批操作：APPROVE-批准, REJECT-拒绝, RETURN-退回修改',
    comment TEXT COMMENT '审批意见',
    approved_at TIMESTAMP NOT NULL COMMENT '审批时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_approval_status_change FOREIGN KEY (status_change_id) REFERENCES student_status_change(id),
    CONSTRAINT fk_approval_approver FOREIGN KEY (approver_id) REFERENCES user(id),
    INDEX idx_approval_change (status_change_id),
    INDEX idx_approval_level (approval_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍异动审批记录表';

