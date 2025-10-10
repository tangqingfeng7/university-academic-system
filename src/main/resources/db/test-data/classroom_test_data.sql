-- ============================================================================
-- 教室资源管理模块测试数据脚本
-- Description: 为教室资源管理模块创建测试数据
-- Author: Academic System Team
-- Date: 2025-01-09
-- 依赖: 需要先执行 V11__create_classroom_tables.sql 和基础测试数据
-- ============================================================================

USE academic_system;

-- ============================================================================
-- 1. 清空现有教室数据（仅用于开发测试环境）
-- ============================================================================
-- 注意：生产环境请注释掉以下DELETE语句
DELETE FROM classroom_usage_log;
DELETE FROM classroom_booking;
DELETE FROM classroom;

-- 重置自增ID
ALTER TABLE classroom AUTO_INCREMENT = 1;
ALTER TABLE classroom_booking AUTO_INCREMENT = 1;
ALTER TABLE classroom_usage_log AUTO_INCREMENT = 1;

-- ============================================================================
-- 2. 插入教室测试数据
-- ============================================================================

-- A栋教学楼 - 普通教室（20间）
INSERT INTO classroom (room_no, building, capacity, type, equipment, status, deleted, created_at, updated_at) VALUES
('A101', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A102', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A103', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A104', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A105', 'A栋教学楼', 100, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A201', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A202', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A203', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A204', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'MAINTENANCE', FALSE, NOW(), NOW()),
('A205', 'A栋教学楼', 100, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A301', 'A栋教学楼', 50, 'LECTURE', '{"投影仪": true, "电脑": false, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A302', 'A栋教学楼', 50, 'LECTURE', '{"投影仪": true, "电脑": false, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A303', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A304', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A305', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A401', 'A栋教学楼', 50, 'LECTURE', '{"投影仪": true, "电脑": false, "音响": false, "白板": true, "空调": false, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A402', 'A栋教学楼', 50, 'LECTURE', '{"投影仪": true, "电脑": false, "音响": false, "白板": true, "空调": false, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A403', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('A404', 'A栋教学楼', 60, 'LECTURE', '{"投影仪": false, "电脑": false, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'DISABLED', FALSE, NOW(), NOW()),
('A405', 'A栋教学楼', 80, 'LECTURE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW());

-- B栋教学楼 - 多媒体教室（10间）
INSERT INTO classroom (room_no, building, capacity, type, equipment, status, deleted, created_at, updated_at) VALUES
('B101', 'B栋教学楼', 120, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B102', 'B栋教学楼', 120, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B103', 'B栋教学楼', 150, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B201', 'B栋教学楼', 100, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B202', 'B栋教学楼', 100, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B203', 'B栋教学楼', 120, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B301', 'B栋教学楼', 80, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B302', 'B栋教学楼', 80, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('B303', 'B栋教学楼', 100, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'MAINTENANCE', FALSE, NOW(), NOW()),
('B401', 'B栋教学楼', 60, 'MULTIMEDIA', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW());

-- C栋实验楼 - 实验室（15间）
INSERT INTO classroom (room_no, building, capacity, type, equipment, status, deleted, created_at, updated_at) VALUES
('C101', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C102', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C103', 'C栋实验楼', 50, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C104', 'C栋实验楼', 50, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C105', 'C栋实验楼', 60, 'LAB', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C201', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C202', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C203', 'C栋实验楼', 50, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C204', 'C栋实验楼', 50, 'LAB', '{"投影仪": false, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C205', 'C栋实验楼', 60, 'LAB', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C301', 'C栋实验楼', 30, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C302', 'C栋实验楼', 30, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C303', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('C304', 'C栋实验楼', 40, 'LAB', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'MAINTENANCE', FALSE, NOW(), NOW()),
('C305', 'C栋实验楼', 50, 'LAB', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW());

-- D栋行政楼 - 会议室（5间）
INSERT INTO classroom (room_no, building, capacity, type, equipment, status, deleted, created_at, updated_at) VALUES
('D101', 'D栋行政楼', 30, 'CONFERENCE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('D102', 'D栋行政楼', 50, 'CONFERENCE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('D201', 'D栋行政楼', 20, 'CONFERENCE', '{"投影仪": true, "电脑": true, "音响": false, "白板": true, "空调": true, "麦克风": false}', 'AVAILABLE', FALSE, NOW(), NOW()),
('D202', 'D栋行政楼', 40, 'CONFERENCE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW()),
('D301', 'D栋行政楼', 100, 'CONFERENCE', '{"投影仪": true, "电脑": true, "音响": true, "白板": true, "空调": true, "麦克风": true}', 'AVAILABLE', FALSE, NOW(), NOW());

-- ============================================================================
-- 3. 插入教室借用测试数据
-- ============================================================================

-- 获取一些教师用户ID用于测试
SET @teacher1_user_id = (SELECT user_id FROM teacher WHERE teacher_no = 'T001' LIMIT 1);
SET @teacher2_user_id = (SELECT user_id FROM teacher WHERE teacher_no = 'T002' LIMIT 1);
SET @teacher3_user_id = (SELECT user_id FROM teacher WHERE teacher_no = 'T003' LIMIT 1);
SET @admin_user_id = (SELECT id FROM user WHERE role = 'ADMIN' LIMIT 1);

-- 已批准的借用（过去一周）
INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, approved_by, approval_comment, approved_at, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'D102'),
    @teacher1_user_id,
    DATE_ADD(NOW(), INTERVAL -5 DAY) + INTERVAL 14 HOUR,
    DATE_ADD(NOW(), INTERVAL -5 DAY) + INTERVAL 16 HOUR,
    '举办学术讲座：人工智能前沿技术分享',
    45,
    'APPROVED',
    @admin_user_id,
    '批准',
    DATE_ADD(NOW(), INTERVAL -6 DAY),
    DATE_ADD(NOW(), INTERVAL -7 DAY),
    DATE_ADD(NOW(), INTERVAL -6 DAY)
WHERE @teacher1_user_id IS NOT NULL;

INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, approved_by, approval_comment, approved_at, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B101'),
    @teacher2_user_id,
    DATE_ADD(NOW(), INTERVAL -3 DAY) + INTERVAL 19 HOUR,
    DATE_ADD(NOW(), INTERVAL -3 DAY) + INTERVAL 21 HOUR,
    '组织学生活动：编程竞赛培训',
    80,
    'APPROVED',
    @admin_user_id,
    '批准',
    DATE_ADD(NOW(), INTERVAL -4 DAY),
    DATE_ADD(NOW(), INTERVAL -5 DAY),
    DATE_ADD(NOW(), INTERVAL -4 DAY)
WHERE @teacher2_user_id IS NOT NULL;

-- 待审批的借用（未来一周）
INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'D301'),
    @teacher1_user_id,
    DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 14 HOUR,
    DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 17 HOUR,
    '举办学术会议：计算机科学前沿研讨会',
    90,
    'PENDING',
    NOW(),
    NOW()
WHERE @teacher1_user_id IS NOT NULL;

INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A105'),
    @teacher2_user_id,
    DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 19 HOUR,
    DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 21 HOUR,
    '组织学生活动：班级联谊晚会',
    100,
    'PENDING',
    DATE_ADD(NOW(), INTERVAL -1 HOUR),
    DATE_ADD(NOW(), INTERVAL -1 HOUR)
WHERE @teacher2_user_id IS NOT NULL;

INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'C105'),
    @teacher3_user_id,
    DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 14 HOUR,
    DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 16 HOUR,
    '举办实验课程：物理实验开放日',
    50,
    'PENDING',
    DATE_ADD(NOW(), INTERVAL -2 HOUR),
    DATE_ADD(NOW(), INTERVAL -2 HOUR)
WHERE @teacher3_user_id IS NOT NULL;

-- 已拒绝的借用
INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, approved_by, approval_comment, approved_at, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B103'),
    @teacher1_user_id,
    DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 14 HOUR,
    DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR,
    '举办商业活动',
    120,
    'REJECTED',
    @admin_user_id,
    '教室不适合举办商业活动',
    DATE_ADD(NOW(), INTERVAL -1 DAY),
    DATE_ADD(NOW(), INTERVAL -2 DAY),
    DATE_ADD(NOW(), INTERVAL -1 DAY)
WHERE @teacher1_user_id IS NOT NULL;

-- 已取消的借用
INSERT INTO classroom_booking (classroom_id, applicant_id, start_time, end_time, purpose, expected_attendees, status, created_at, updated_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A201'),
    @teacher2_user_id,
    DATE_ADD(NOW(), INTERVAL 4 DAY) + INTERVAL 10 HOUR,
    DATE_ADD(NOW(), INTERVAL 4 DAY) + INTERVAL 12 HOUR,
    '取消的讲座',
    60,
    'CANCELLED',
    DATE_ADD(NOW(), INTERVAL -3 HOUR),
    DATE_ADD(NOW(), INTERVAL -3 HOUR)
WHERE @teacher2_user_id IS NOT NULL;

-- ============================================================================
-- 4. 插入教室使用记录测试数据
-- ============================================================================

-- 为已批准的借用创建使用记录
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time)
SELECT 
    cb.classroom_id,
    'BOOKING',
    cb.id,
    cb.start_time,
    cb.end_time
FROM classroom_booking cb
WHERE cb.status = 'APPROVED';

-- 为现有的课程开课创建使用记录（假设course_offering表中有数据）
-- 注意：这里需要根据实际的course_offering数据来调整
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time)
SELECT 
    c.id AS classroom_id,
    'COURSE',
    co.id AS reference_id,
    -- 生成本周的上课时间（周一到周五）
    CASE 
        WHEN WEEKDAY(NOW()) = 0 THEN DATE(NOW()) + INTERVAL 8 HOUR  -- 周一
        WHEN WEEKDAY(NOW()) = 1 THEN DATE(NOW()) + INTERVAL 8 HOUR  -- 周二
        WHEN WEEKDAY(NOW()) = 2 THEN DATE(NOW()) + INTERVAL 8 HOUR  -- 周三
        WHEN WEEKDAY(NOW()) = 3 THEN DATE(NOW()) + INTERVAL 8 HOUR  -- 周四
        ELSE DATE(NOW()) + INTERVAL 8 HOUR  -- 周五
    END AS start_time,
    CASE 
        WHEN WEEKDAY(NOW()) = 0 THEN DATE(NOW()) + INTERVAL 10 HOUR
        WHEN WEEKDAY(NOW()) = 1 THEN DATE(NOW()) + INTERVAL 10 HOUR
        WHEN WEEKDAY(NOW()) = 2 THEN DATE(NOW()) + INTERVAL 10 HOUR
        WHEN WEEKDAY(NOW()) = 3 THEN DATE(NOW()) + INTERVAL 10 HOUR
        ELSE DATE(NOW()) + INTERVAL 10 HOUR
    END AS end_time
FROM course_offering co
CROSS JOIN classroom c
WHERE co.location LIKE CONCAT('%', c.room_no, '%')
  AND co.semester_id = (SELECT id FROM semester WHERE academic_year = '2024-2025' AND semester_type = 2 LIMIT 1)
LIMIT 100;  -- 限制生成的记录数量

-- 为考试创建使用记录（假设exam和exam_room表中有数据）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time)
SELECT 
    c.id AS classroom_id,
    'EXAM',
    e.id AS reference_id,
    e.start_time,
    e.end_time
FROM exam e
INNER JOIN exam_room er ON e.id = er.exam_id
CROSS JOIN classroom c
WHERE er.location LIKE CONCAT('%', c.room_no, '%')
  AND e.start_time > DATE_SUB(NOW(), INTERVAL 30 DAY)
LIMIT 50;  -- 限制生成的记录数量

-- ============================================================================
-- 5. 验证数据完整性
-- ============================================================================

-- 显示插入的数据统计
SELECT '教室总数' AS metric, COUNT(*) AS count FROM classroom WHERE deleted = FALSE
UNION ALL
SELECT '可用教室数', COUNT(*) FROM classroom WHERE status = 'AVAILABLE' AND deleted = FALSE
UNION ALL
SELECT '普通教室数', COUNT(*) FROM classroom WHERE type = 'LECTURE' AND deleted = FALSE
UNION ALL
SELECT '多媒体教室数', COUNT(*) FROM classroom WHERE type = 'MULTIMEDIA' AND deleted = FALSE
UNION ALL
SELECT '实验室数', COUNT(*) FROM classroom WHERE type = 'LAB' AND deleted = FALSE
UNION ALL
SELECT '会议室数', COUNT(*) FROM classroom WHERE type = 'CONFERENCE' AND deleted = FALSE
UNION ALL
SELECT '借用申请总数', COUNT(*) FROM classroom_booking
UNION ALL
SELECT '待审批申请数', COUNT(*) FROM classroom_booking WHERE status = 'PENDING'
UNION ALL
SELECT '使用记录总数', COUNT(*) FROM classroom_usage_log;

-- ============================================================================
-- 完成
-- ============================================================================

COMMIT;

