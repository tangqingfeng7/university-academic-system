-- =============================================
-- 考勤管理模块表结构
-- Version: V29
-- Author: Academic System Team
-- =============================================

-- =============================================
-- 1. 考勤记录表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考勤记录ID',
    offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    attendance_time TIME NOT NULL COMMENT '考勤时间',
    method VARCHAR(20) NOT NULL COMMENT '考勤方式：MANUAL手动点名, QRCODE扫码签到, LOCATION定位签到',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '记录状态：IN_PROGRESS进行中, SUBMITTED已提交, CANCELLED已取消',
    total_students INT COMMENT '应到人数',
    present_count INT DEFAULT 0 COMMENT '实到人数',
    absent_count INT DEFAULT 0 COMMENT '缺勤人数',
    late_count INT DEFAULT 0 COMMENT '迟到人数',
    leave_count INT DEFAULT 0 COMMENT '请假人数',
    attendance_rate DECIMAL(5,2) COMMENT '出勤率',
    qr_token VARCHAR(100) COMMENT '二维码令牌',
    qr_expire_time DATETIME COMMENT '二维码过期时间',
    latitude DECIMAL(10,8) COMMENT '纬度',
    longitude DECIMAL(11,8) COMMENT '经度',
    geofence_radius INT COMMENT '地理围栏半径（米）',
    remarks TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_offering_date (offering_id, attendance_date),
    INDEX idx_teacher_date (teacher_id, attendance_date),
    INDEX idx_status (status),
    INDEX idx_qr_token (qr_token),
    CONSTRAINT fk_attendance_record_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id),
    CONSTRAINT fk_attendance_record_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- =============================================
-- 2. 考勤明细表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考勤明细ID',
    record_id BIGINT NOT NULL COMMENT '考勤记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ABSENT' COMMENT '考勤状态：PRESENT出勤, LATE迟到, EARLY_LEAVE早退, LEAVE请假, ABSENT旷课',
    checkin_time DATETIME COMMENT '签到时间',
    checkin_latitude DECIMAL(10,8) COMMENT '签到纬度',
    checkin_longitude DECIMAL(11,8) COMMENT '签到经度',
    is_makeup BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否补签',
    remarks TEXT COMMENT '备注',
    modified_by BIGINT COMMENT '修改人ID',
    modify_reason TEXT COMMENT '修改原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_student (record_id, student_id),
    INDEX idx_student_status (student_id, status),
    CONSTRAINT fk_attendance_detail_record FOREIGN KEY (record_id) REFERENCES attendance_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_detail_student FOREIGN KEY (student_id) REFERENCES student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤明细表';

-- =============================================
-- 3. 考勤统计表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    total_classes INT NOT NULL DEFAULT 0 COMMENT '总课次',
    present_count INT NOT NULL DEFAULT 0 COMMENT '出勤次数',
    late_count INT NOT NULL DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT NOT NULL DEFAULT 0 COMMENT '早退次数',
    leave_count INT NOT NULL DEFAULT 0 COMMENT '请假次数',
    absent_count INT NOT NULL DEFAULT 0 COMMENT '旷课次数',
    attendance_rate DECIMAL(5,2) COMMENT '出勤率',
    last_updated DATETIME COMMENT '最后更新时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_student_offering (student_id, offering_id),
    INDEX idx_offering_rate (offering_id, attendance_rate),
    CONSTRAINT fk_attendance_statistics_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_attendance_statistics_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计表';

-- =============================================
-- 4. 考勤预警表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_warning (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    warning_type VARCHAR(30) NOT NULL COMMENT '预警类型：STUDENT_ABSENT学生旷课, COURSE_LOW_RATE课程出勤率低, TEACHER_NO_ATTENDANCE教师未考勤',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：STUDENT, COURSE, TEACHER',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    target_name VARCHAR(100) COMMENT '目标名称',
    offering_id BIGINT COMMENT '相关课程ID',
    warning_level INT NOT NULL COMMENT '预警级别：1轻度, 2中度, 3严重',
    warning_message TEXT COMMENT '预警消息',
    warning_data JSON COMMENT '预警数据',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING待处理, HANDLED已处理, IGNORED已忽略',
    handled_by BIGINT COMMENT '处理人ID',
    handled_at DATETIME COMMENT '处理时间',
    handle_comment TEXT COMMENT '处理意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_target (target_type, target_id),
    INDEX idx_status_level (status, warning_level),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_attendance_warning_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤预警表';

-- =============================================
-- 5. 考勤申请表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    request_type VARCHAR(20) NOT NULL COMMENT '申请类型：MAKEUP补签, APPEAL申诉',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    detail_id BIGINT NOT NULL COMMENT '考勤明细ID',
    reason TEXT NOT NULL COMMENT '申请原因',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING待审批, APPROVED已批准, REJECTED已拒绝',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approval_comment TEXT COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_status (student_id, status),
    INDEX idx_detail (detail_id),
    CONSTRAINT fk_attendance_request_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_attendance_request_detail FOREIGN KEY (detail_id) REFERENCES attendance_detail(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤申请表';

-- =============================================
-- 6. 考勤配置表
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型：INTEGER, DOUBLE, BOOLEAN, STRING',
    description TEXT COMMENT '配置描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统配置',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤配置表';

-- =============================================
-- 初始化默认配置数据
-- =============================================
INSERT INTO attendance_config (config_key, config_value, config_type, description, is_system) VALUES
-- 签到时间配置
('attendance.qrcode.expire_minutes', '5', 'INTEGER', '二维码签到有效期（分钟）', TRUE),
('attendance.early_checkin_minutes', '5', 'INTEGER', '允许提前签到时间（分钟）', TRUE),
('attendance.late_threshold_minutes', '5', 'INTEGER', '迟到判定时间（分钟）', TRUE),
('attendance.max_late_minutes', '15', 'INTEGER', '最晚签到时间（分钟）', TRUE),

-- 定位签到配置
('attendance.location.geofence_radius', '100', 'INTEGER', '地理围栏半径（米）', TRUE),

-- 预警配置
('attendance.warning.absent_threshold', '3', 'INTEGER', '旷课次数预警阈值', TRUE),
('attendance.warning.absent_rate_threshold', '0.33', 'DOUBLE', '旷课比例预警阈值', TRUE),
('attendance.warning.low_attendance_rate', '0.70', 'DOUBLE', '课程出勤率预警阈值', TRUE),
('attendance.warning.teacher_no_attendance_days', '7', 'INTEGER', '教师未考勤提醒天数', TRUE),

-- 功能开关
('attendance.method.manual.enabled', 'true', 'BOOLEAN', '启用手动点名', TRUE),
('attendance.method.qrcode.enabled', 'true', 'BOOLEAN', '启用扫码签到', TRUE),
('attendance.method.location.enabled', 'true', 'BOOLEAN', '启用定位签到', TRUE),

-- 缓存配置
('attendance.cache.expire_minutes', '30', 'INTEGER', '统计数据缓存过期时间（分钟）', TRUE);

