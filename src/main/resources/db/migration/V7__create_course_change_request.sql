-- 调课申请表
CREATE TABLE course_change_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL COMMENT '申请教师ID',
    teacher_name VARCHAR(50) NOT NULL COMMENT '申请教师姓名',
    offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    original_schedule TEXT NOT NULL COMMENT '原时间安排（JSON格式）',
    new_schedule TEXT NOT NULL COMMENT '新时间安排（JSON格式）',
    reason TEXT NOT NULL COMMENT '调课原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approval_comment TEXT COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    FOREIGN KEY (offering_id) REFERENCES course_offering(id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT='调课申请表';

