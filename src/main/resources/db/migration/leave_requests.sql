-- =============================================
-- 请假管理表
-- =============================================

CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请假ID',
    applicant_type VARCHAR(20) NOT NULL COMMENT '申请人类型：STUDENT学生, TEACHER教师',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    applicant_no VARCHAR(20) NOT NULL COMMENT '申请人学号/工号',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型：SICK病假, PERSONAL事假, OTHER其他',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    days INT NOT NULL COMMENT '请假天数',
    reason TEXT NOT NULL COMMENT '请假原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态：PENDING待审批, APPROVED已批准, REJECTED已拒绝, CANCELLED已取消',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approval_comment TEXT COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_applicant (applicant_type, applicant_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 插入测试数据
INSERT INTO leave_request (applicant_type, applicant_id, applicant_name, applicant_no, leave_type, start_date, end_date, days, reason, status, approver_id, approver_name, approval_comment, approval_time)
VALUES
-- 学生请假（待审批）
('STUDENT', 1, '张伟', 'S000001', 'SICK', '2025-10-10', '2025-10-12', 3, '感冒发烧，需要在家休息', 'PENDING', NULL, NULL, NULL, NULL),
('STUDENT', 2, '李娜', 'S000002', 'PERSONAL', '2025-10-15', '2025-10-15', 1, '家里有事需要处理', 'PENDING', NULL, NULL, NULL, NULL),

-- 学生请假（已批准）
('STUDENT', 1, '张伟', 'S000001', 'PERSONAL', '2025-10-01', '2025-10-02', 2, '回家探亲', 'APPROVED', 1, 'admin', '同意请假，注意安全', '2025-09-30 10:00:00'),
('STUDENT', 3, '王强', 'S000003', 'SICK', '2025-09-25', '2025-09-27', 3, '急性肠胃炎', 'APPROVED', 1, 'admin', '同意请假，好好休息', '2025-09-24 14:30:00'),

-- 学生请假（已拒绝）
('STUDENT', 4, '赵敏', 'S000004', 'PERSONAL', '2025-10-20', '2025-10-25', 6, '想回家玩', 'REJECTED', 1, 'admin', '请假时间过长，且理由不充分', '2025-10-08 09:00:00'),

-- 教师请假（待审批）
('TEACHER', 1, '张伟', 'T001', 'PERSONAL', '2025-10-18', '2025-10-19', 2, '参加学术会议', 'PENDING', NULL, NULL, NULL, NULL),
('TEACHER', 2, '李明', 'T002', 'SICK', '2025-10-11', '2025-10-13', 3, '身体不适需要休息', 'PENDING', NULL, NULL, NULL, NULL),

-- 教师请假（已批准）
('TEACHER', 1, '张伟', 'T001', 'PERSONAL', '2025-09-28', '2025-09-30', 3, '参加外校学术交流活动', 'APPROVED', 1, 'admin', '同意请假，做好课程调课安排', '2025-09-27 11:00:00'),

-- 教师请假（已拒绝）
('TEACHER', 3, '王芳', 'T003', 'PERSONAL', '2025-10-09', '2025-10-12', 4, '私人事务', 'REJECTED', 1, 'admin', '期中考试期间，不宜请假', '2025-10-07 16:00:00');

