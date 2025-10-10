-- ============================================================================
-- 教室资源管理模块数据库迁移脚本
-- Version: V11
-- Description: 创建教室资源管理相关表（教室、教室借用、教室使用记录）
-- Author: Academic System Team
-- Date: 2025-01-09
-- ============================================================================

USE academic_system;

-- ============================================================================
-- 1. 教室表 (classroom)
-- ============================================================================
CREATE TABLE IF NOT EXISTS classroom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    room_no VARCHAR(20) UNIQUE NOT NULL COMMENT '教室编号（如：A101、B205）',
    building VARCHAR(50) NOT NULL COMMENT '楼栋（如：A栋教学楼、实验楼）',
    capacity INT NOT NULL COMMENT '容量（可容纳人数）',
    type VARCHAR(20) NOT NULL COMMENT '教室类型（LECTURE-普通教室, LAB-实验室, MULTIMEDIA-多媒体教室, CONFERENCE-会议室）',
    equipment TEXT COMMENT '设备信息（JSON格式：投影仪、电脑、音响等）',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态（AVAILABLE-可用, MAINTENANCE-维修中, DISABLED-停用）',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（软删除：0-否，1-是）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_classroom_room_no (room_no),
    INDEX idx_classroom_building (building),
    INDEX idx_classroom_type (type),
    INDEX idx_classroom_status (status),
    INDEX idx_classroom_deleted (deleted),
    INDEX idx_classroom_capacity (capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';

-- ============================================================================
-- 2. 教室借用表 (classroom_booking)
-- ============================================================================
CREATE TABLE IF NOT EXISTS classroom_booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID（用户ID）',
    start_time DATETIME NOT NULL COMMENT '借用开始时间',
    end_time DATETIME NOT NULL COMMENT '借用结束时间',
    purpose VARCHAR(200) NOT NULL COMMENT '借用目的',
    expected_attendees INT COMMENT '预计参加人数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态（PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消）',
    approved_by BIGINT COMMENT '审批人ID',
    approval_comment TEXT COMMENT '审批意见',
    approved_at DATETIME COMMENT '审批时间',
    remarks VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE RESTRICT,
    FOREIGN KEY (applicant_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (approved_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_booking_classroom (classroom_id),
    INDEX idx_booking_applicant (applicant_id),
    INDEX idx_booking_status (status),
    INDEX idx_booking_time (start_time, end_time),
    INDEX idx_booking_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室借用表';

-- ============================================================================
-- 3. 教室使用记录表 (classroom_usage_log)
-- ============================================================================
CREATE TABLE IF NOT EXISTS classroom_usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    type VARCHAR(20) NOT NULL COMMENT '使用类型（COURSE-上课, EXAM-考试, BOOKING-借用, OTHER-其他）',
    reference_id BIGINT COMMENT '关联ID（课程开课ID、考试ID、借用ID等）',
    start_time DATETIME NOT NULL COMMENT '使用开始时间',
    end_time DATETIME NOT NULL COMMENT '使用结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    INDEX idx_usage_classroom (classroom_id),
    INDEX idx_usage_type (type),
    INDEX idx_usage_reference (reference_id),
    INDEX idx_usage_time (start_time, end_time),
    INDEX idx_usage_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室使用记录表';

-- ============================================================================
-- 数据校验约束
-- ============================================================================

-- 教室容量必须大于0
ALTER TABLE classroom 
    ADD CONSTRAINT chk_classroom_capacity CHECK (capacity > 0);

-- 借用结束时间必须晚于开始时间
ALTER TABLE classroom_booking
    ADD CONSTRAINT chk_booking_time CHECK (end_time > start_time);

-- 预计人数必须大于0
ALTER TABLE classroom_booking
    ADD CONSTRAINT chk_expected_attendees CHECK (expected_attendees IS NULL OR expected_attendees > 0);

-- 使用记录结束时间必须晚于开始时间
ALTER TABLE classroom_usage_log
    ADD CONSTRAINT chk_usage_time CHECK (end_time > start_time);

-- ============================================================================
-- 创建视图：方便查询教室使用情况和统计信息
-- ============================================================================

-- 教室使用率统计视图
CREATE OR REPLACE VIEW v_classroom_utilization_stats AS
SELECT 
    c.id AS classroom_id,
    c.room_no,
    c.building,
    c.capacity,
    c.type,
    c.status,
    COUNT(cul.id) AS total_usage_count,
    SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) AS total_usage_minutes,
    SUM(CASE WHEN cul.type = 'COURSE' THEN 1 ELSE 0 END) AS course_count,
    SUM(CASE WHEN cul.type = 'EXAM' THEN 1 ELSE 0 END) AS exam_count,
    SUM(CASE WHEN cul.type = 'BOOKING' THEN 1 ELSE 0 END) AS booking_count,
    SUM(CASE WHEN cul.type = 'OTHER' THEN 1 ELSE 0 END) AS other_count
FROM classroom c
LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id
WHERE c.deleted = FALSE
GROUP BY c.id, c.room_no, c.building, c.capacity, c.type, c.status;

-- 教室借用申请统计视图
CREATE OR REPLACE VIEW v_classroom_booking_stats AS
SELECT 
    c.id AS classroom_id,
    c.room_no,
    c.building,
    COUNT(cb.id) AS total_bookings,
    SUM(CASE WHEN cb.status = 'PENDING' THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN cb.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN cb.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_count,
    SUM(CASE WHEN cb.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count
FROM classroom c
LEFT JOIN classroom_booking cb ON c.id = cb.classroom_id
WHERE c.deleted = FALSE
GROUP BY c.id, c.room_no, c.building;

-- 教室当前状态视图（包含今日使用情况）
CREATE OR REPLACE VIEW v_classroom_current_status AS
SELECT 
    c.id AS classroom_id,
    c.room_no,
    c.building,
    c.capacity,
    c.type,
    c.status,
    c.equipment,
    COUNT(DISTINCT cul.id) AS today_usage_count,
    SUM(CASE WHEN cul.start_time <= NOW() AND cul.end_time >= NOW() THEN 1 ELSE 0 END) AS currently_in_use
FROM classroom c
LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id 
    AND DATE(cul.start_time) = CURDATE()
WHERE c.deleted = FALSE
GROUP BY c.id, c.room_no, c.building, c.capacity, c.type, c.status, c.equipment;

-- 待审批借用申请视图（包含申请人和教室信息）
CREATE OR REPLACE VIEW v_pending_classroom_bookings AS
SELECT 
    cb.id AS booking_id,
    cb.classroom_id,
    c.room_no,
    c.building,
    c.capacity,
    cb.applicant_id,
    u.username AS applicant_username,
    COALESCE(t.name, s.name) AS applicant_name,
    cb.start_time,
    cb.end_time,
    cb.purpose,
    cb.expected_attendees,
    cb.remarks,
    cb.created_at
FROM classroom_booking cb
INNER JOIN classroom c ON cb.classroom_id = c.id
INNER JOIN user u ON cb.applicant_id = u.id
LEFT JOIN teacher t ON u.id = t.user_id
LEFT JOIN student s ON u.id = s.user_id
WHERE cb.status = 'PENDING'
ORDER BY cb.created_at ASC;

-- ============================================================================
-- 创建存储过程：自动维护教室使用记录
-- ============================================================================

-- 存储过程：当借用申请被批准时，自动创建使用记录
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS sp_create_usage_log_on_booking_approval(
    IN p_booking_id BIGINT
)
BEGIN
    DECLARE v_classroom_id BIGINT;
    DECLARE v_start_time DATETIME;
    DECLARE v_end_time DATETIME;
    
    -- 获取借用信息
    SELECT classroom_id, start_time, end_time
    INTO v_classroom_id, v_start_time, v_end_time
    FROM classroom_booking
    WHERE id = p_booking_id AND status = 'APPROVED';
    
    -- 如果找到了批准的借用记录，创建使用记录
    IF v_classroom_id IS NOT NULL THEN
        -- 先删除可能存在的旧记录（防止重复）
        DELETE FROM classroom_usage_log 
        WHERE type = 'BOOKING' AND reference_id = p_booking_id;
        
        -- 插入新的使用记录
        INSERT INTO classroom_usage_log (
            classroom_id, 
            type, 
            reference_id, 
            start_time, 
            end_time
        ) VALUES (
            v_classroom_id,
            'BOOKING',
            p_booking_id,
            v_start_time,
            v_end_time
        );
    END IF;
END //

-- 存储过程：当借用被取消时，删除使用记录
CREATE PROCEDURE IF NOT EXISTS sp_delete_usage_log_on_booking_cancellation(
    IN p_booking_id BIGINT
)
BEGIN
    DELETE FROM classroom_usage_log 
    WHERE type = 'BOOKING' AND reference_id = p_booking_id;
END //

DELIMITER ;

-- ============================================================================
-- 创建触发器：自动维护使用记录
-- ============================================================================

-- 注意：由于借用审批在应用层处理，这里不创建触发器
-- 应用层在审批通过时应调用 sp_create_usage_log_on_booking_approval
-- 应用层在取消借用时应调用 sp_delete_usage_log_on_booking_cancellation

-- ============================================================================
-- 数据初始化说明
-- ============================================================================
-- 实际的测试数据将在 classroom_test_data.sql 中添加

COMMIT;

