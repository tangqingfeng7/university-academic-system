-- =============================================
-- 考勤管理模块测试数据
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清空现有数据（按依赖顺序）
DELETE FROM attendance_request;
DELETE FROM attendance_detail;
DELETE FROM attendance_warning;
DELETE FROM attendance_statistics;
DELETE FROM attendance_record;

-- =============================================
-- 插入考勤记录测试数据
-- =============================================

-- 教师1的考勤记录（手动点名，已提交）
INSERT INTO attendance_record (id, offering_id, teacher_id, attendance_date, attendance_time, method, status, total_students, present_count, absent_count, late_count, leave_count, attendance_rate, remarks)
VALUES 
(1, 1, 1, '2025-10-08', '08:00:00', 'MANUAL', 'SUBMITTED', 50, 45, 2, 2, 1, 94.00, '第1次考勤'),
(2, 1, 1, '2025-10-10', '08:00:00', 'MANUAL', 'SUBMITTED', 50, 47, 1, 1, 1, 96.00, '第2次考勤'),
(3, 1, 1, '2025-10-13', '08:00:00', 'MANUAL', 'IN_PROGRESS', 50, 0, 50, 0, 0, 0.00, '进行中的考勤');

-- 教师1的考勤记录（扫码签到，已提交）
INSERT INTO attendance_record (id, offering_id, teacher_id, attendance_date, attendance_time, method, status, total_students, present_count, absent_count, late_count, leave_count, attendance_rate, qr_token, qr_expire_time)
VALUES 
(4, 2, 1, '2025-10-09', '10:00:00', 'QRCODE', 'SUBMITTED', 45, 42, 1, 1, 1, 95.56, NULL, NULL);

-- 教师2的考勤记录（定位签到，已提交）
INSERT INTO attendance_record (id, offering_id, teacher_id, attendance_date, attendance_time, method, status, total_students, present_count, absent_count, late_count, leave_count, attendance_rate, latitude, longitude, geofence_radius)
VALUES 
(5, 3, 2, '2025-10-09', '14:00:00', 'LOCATION', 'SUBMITTED', 40, 38, 1, 1, 0, 97.50, 39.9042, 116.4074, 100);

-- =============================================
-- 插入考勤明细测试数据
-- =============================================

-- 考勤记录1的明细（手动点名）
INSERT INTO attendance_detail (record_id, student_id, status, checkin_time, is_makeup, remarks)
VALUES
-- 出勤
(1, 1, 'PRESENT', NULL, FALSE, NULL),
(1, 2, 'PRESENT', NULL, FALSE, NULL),
(1, 3, 'PRESENT', NULL, FALSE, NULL),
-- 迟到
(1, 4, 'LATE', NULL, FALSE, '迟到5分钟'),
(1, 5, 'LATE', NULL, FALSE, '迟到3分钟'),
-- 旷课
(1, 6, 'ABSENT', NULL, FALSE, NULL),
(1, 7, 'ABSENT', NULL, FALSE, NULL),
-- 请假
(1, 8, 'LEAVE', NULL, FALSE, '病假');

-- 考勤记录4的明细（扫码签到）
INSERT INTO attendance_detail (record_id, student_id, status, checkin_time, is_makeup)
VALUES
-- 扫码出勤
(4, 1, 'PRESENT', '2025-10-09 09:58:00', FALSE),
(4, 2, 'PRESENT', '2025-10-09 10:02:00', FALSE),
-- 扫码迟到
(4, 3, 'LATE', '2025-10-09 10:08:00', FALSE),
-- 未签到（旷课）
(4, 4, 'ABSENT', NULL, FALSE);

-- 考勤记录5的明细（定位签到）
INSERT INTO attendance_detail (record_id, student_id, status, checkin_time, checkin_latitude, checkin_longitude, is_makeup)
VALUES
-- 定位出勤
(5, 9, 'PRESENT', '2025-10-09 13:58:00', 39.9040, 116.4072, FALSE),
(5, 10, 'PRESENT', '2025-10-09 14:01:00', 39.9041, 116.4073, FALSE),
-- 定位迟到
(5, 11, 'LATE', '2025-10-09 14:07:00', 39.9042, 116.4075, FALSE),
-- 未签到
(5, 12, 'ABSENT', NULL, NULL, NULL, FALSE);

-- =============================================
-- 插入考勤统计测试数据
-- =============================================

INSERT INTO attendance_statistics (student_id, offering_id, total_classes, present_count, late_count, early_leave_count, leave_count, absent_count, attendance_rate, last_updated)
VALUES
-- 学生1的统计
(1, 1, 2, 2, 0, 0, 0, 0, 100.00, NOW()),
(1, 2, 1, 1, 0, 0, 0, 0, 100.00, NOW()),

-- 学生4的统计（有旷课）
(4, 1, 2, 1, 1, 0, 0, 0, 100.00, NOW()),
(4, 2, 1, 0, 0, 0, 0, 1, 0.00, NOW()),

-- 学生6的统计（多次旷课）
(6, 1, 2, 0, 0, 0, 0, 2, 0.00, NOW()),

-- 学生7的统计（多次旷课）
(7, 1, 2, 0, 0, 0, 0, 2, 0.00, NOW());

-- =============================================
-- 插入考勤预警测试数据
-- =============================================

INSERT INTO attendance_warning (warning_type, target_type, target_id, target_name, offering_id, warning_level, warning_message, status)
VALUES
-- 学生旷课预警
('STUDENT_ABSENT', 'STUDENT', 6, '王强', 1, 2, '学生王强在《高等数学》中已旷课2次，请关注！', 'PENDING'),
('STUDENT_ABSENT', 'STUDENT', 7, '赵敏', 1, 2, '学生赵敏在《高等数学》中已旷课2次，请关注！', 'PENDING'),

-- 课程出勤率预警
('COURSE_LOW_RATE', 'COURSE', 1, '高等数学', 1, 2, '课程《高等数学》的平均出勤率为65.00%，低于标准（70%），请关注！', 'PENDING');

-- =============================================
-- 插入考勤申请测试数据
-- =============================================

INSERT INTO attendance_request (id, request_type, student_id, detail_id, reason, attachment_url, status, approver_id, approver_name, approval_comment, approval_time)
VALUES
-- 待审批的补签申请
(1, 'MAKEUP', 4, 1, '昨天忘记签到了，有课堂笔记为证', NULL, 'PENDING', NULL, NULL, NULL, NULL),

-- 已批准的补签申请
(2, 'MAKEUP', 6, 2, '手机没电无法签到', '/uploads/battery_proof.jpg', 'APPROVED', 1, '张伟', '情况属实，同意补签', '2025-10-10 10:00:00'),

-- 已拒绝的申诉
(3, 'APPEAL', 7, 3, '我明明来了，可能是系统问题', NULL, 'REJECTED', 1, '张伟', '经查课堂点名记录，确实未到', '2025-10-11 15:00:00');

-- =============================================
-- 重置自增ID
-- =============================================

ALTER TABLE attendance_record AUTO_INCREMENT = 100;
ALTER TABLE attendance_detail AUTO_INCREMENT = 1000;
ALTER TABLE attendance_statistics AUTO_INCREMENT = 100;
ALTER TABLE attendance_warning AUTO_INCREMENT = 100;
ALTER TABLE attendance_request AUTO_INCREMENT = 100;

