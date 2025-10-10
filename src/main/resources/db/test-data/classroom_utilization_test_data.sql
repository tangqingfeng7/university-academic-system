-- ============================================================================
-- 教室使用率统计测试数据脚本
-- Description: 为教室使用率统计页面生成丰富的测试数据
-- Author: Academic System Team
-- Date: 2025-10-10
-- 用途: 生成最近30天的教室使用记录，涵盖各种使用率场景
-- ============================================================================

USE academic_system;

-- ============================================================================
-- 1. 清空现有使用记录（仅保留教室和借用申请）
-- ============================================================================
DELETE FROM classroom_usage_log WHERE created_at > DATE_SUB(NOW(), INTERVAL 60 DAY);

-- ============================================================================
-- 2. 生成高使用率教室数据（使用率 90-100%）
-- ============================================================================
-- A101, A102, A103, B101, B102（这些教室使用率最高）

-- 为A101生成30天的密集使用记录（每天8:00-20:00，每2小时一节课）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A101' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    1 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16 UNION SELECT 18
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;  -- 只在工作日

-- 为A102生成类似的高使用率数据
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A102' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    2 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 13 UNION SELECT 15 UNION SELECT 19
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- 为A103生成高使用率数据（包含考试）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A103' LIMIT 1) AS classroom_id,
    CASE WHEN hour = 19 THEN 'EXAM' ELSE 'COURSE' END AS type,
    3 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16 UNION SELECT 19
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- B101（多媒体教室，高使用率）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B101' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    4 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16 UNION SELECT 18
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- B102（多媒体教室，包含周末借用）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B102' LIMIT 1) AS classroom_id,
    CASE WHEN WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) >= 5 THEN 'BOOKING' ELSE 'COURSE' END AS type,
    5 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16 UNION SELECT 19
) hours;

-- ============================================================================
-- 3. 生成正常使用率教室数据（使用率 60-90%）
-- ============================================================================
-- A104, A105, A201, A202, B201, B202, C101, C102

-- A104（正常使用率，每天4节课）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A104' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    6 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- A105（正常使用率，混合使用类型）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A105' LIMIT 1) AS classroom_id,
    CASE 
        WHEN hour = 19 THEN 'EXAM'
        WHEN hour = 18 THEN 'BOOKING'
        ELSE 'COURSE' 
    END AS type,
    7 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 18
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- A201, A202（正常使用率）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A201' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    8 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'A202' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    9 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 10 AS hour UNION SELECT 14 UNION SELECT 16
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- B201, B202（多媒体教室，正常使用率）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B201' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    10 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 10 UNION SELECT 14 UNION SELECT 16
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'B202' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    11 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 10 AS hour UNION SELECT 14 UNION SELECT 16
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- C101, C102（实验室，正常使用率）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'C101' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    12 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 3 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 14
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'C102' LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    13 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 3 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 8 AS hour UNION SELECT 14
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- ============================================================================
-- 4. 生成中等使用率教室数据（使用率 30-60%）
-- ============================================================================
-- A203, A301, A302, B203, C103, C201

-- 每天2-3节课
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = room LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    14 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 2 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 'A203' AS room UNION SELECT 'A301' UNION SELECT 'A302' 
    UNION SELECT 'B203' UNION SELECT 'C103' UNION SELECT 'C201'
) rooms
CROSS JOIN (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 10 AS hour UNION SELECT 14
) hours
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- ============================================================================
-- 5. 生成低使用率教室数据（使用率 <30%）
-- ============================================================================
-- A303, A304, A401, A402, A403, A405, B301, B302, B401, C202, C203, C301, C302, C303, C305

-- 每天只有1节课或更少
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = room LIMIT 1) AS classroom_id,
    'COURSE' AS type,
    15 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 10 HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 12 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 10 HOUR) AS created_at
FROM (
    SELECT 'A303' AS room UNION SELECT 'A304' UNION SELECT 'A401' UNION SELECT 'A402' 
    UNION SELECT 'A403' UNION SELECT 'A405' UNION SELECT 'B301' UNION SELECT 'B302'
    UNION SELECT 'B401' UNION SELECT 'C202' UNION SELECT 'C203' UNION SELECT 'C301'
    UNION SELECT 'C302' UNION SELECT 'C303' UNION SELECT 'C305'
) rooms
CROSS JOIN (
    SELECT 0 AS seq UNION SELECT 2 UNION SELECT 4 UNION SELECT 6 UNION SELECT 8
    UNION SELECT 10 UNION SELECT 12 UNION SELECT 14 UNION SELECT 16 UNION SELECT 18
    UNION SELECT 20 UNION SELECT 22 UNION SELECT 24 UNION SELECT 26 UNION SELECT 28
) days
WHERE WEEKDAY(DATE_SUB(CURDATE(), INTERVAL seq DAY)) < 5;

-- ============================================================================
-- 6. 生成会议室使用数据（主要是借用）
-- ============================================================================
-- D101, D102, D201, D202, D301

-- D301（大会议室，使用率高）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'D301' LIMIT 1) AS classroom_id,
    'BOOKING' AS type,
    16 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour + 3 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL hour HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
    UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) days
CROSS JOIN (
    SELECT 9 AS hour UNION SELECT 14
) hours;

-- D102（中会议室）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = 'D102' LIMIT 1) AS classroom_id,
    'BOOKING' AS type,
    17 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 14 HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 17 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 14 HOUR) AS created_at
FROM (
    SELECT 0 AS seq UNION SELECT 3 UNION SELECT 5 UNION SELECT 7 UNION SELECT 10
    UNION SELECT 12 UNION SELECT 14 UNION SELECT 17 UNION SELECT 19 UNION SELECT 21
    UNION SELECT 24 UNION SELECT 26 UNION SELECT 28
) days;

-- D101, D201（小会议室，低使用率）
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = room LIMIT 1) AS classroom_id,
    'BOOKING' AS type,
    18 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 14 HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 16 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 14 HOUR) AS created_at
FROM (
    SELECT 'D101' AS room UNION SELECT 'D201' UNION SELECT 'D202'
) rooms
CROSS JOIN (
    SELECT 1 AS seq UNION SELECT 5 UNION SELECT 10 UNION SELECT 15 
    UNION SELECT 20 UNION SELECT 25
) days;

-- ============================================================================
-- 7. 添加一些考试使用记录
-- ============================================================================
INSERT INTO classroom_usage_log (classroom_id, type, reference_id, start_time, end_time, created_at)
SELECT 
    (SELECT id FROM classroom WHERE room_no = room LIMIT 1) AS classroom_id,
    'EXAM' AS type,
    19 AS reference_id,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 19 HOUR) AS start_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 21 HOUR) AS end_time,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL seq DAY), INTERVAL 19 HOUR) AS created_at
FROM (
    SELECT 'A101' AS room UNION SELECT 'A102' UNION SELECT 'A103' UNION SELECT 'A104'
    UNION SELECT 'A105' UNION SELECT 'B101' UNION SELECT 'B102' UNION SELECT 'B201'
) rooms
CROSS JOIN (
    SELECT 5 AS seq UNION SELECT 10 UNION SELECT 15 UNION SELECT 20 UNION SELECT 25
) days;

-- ============================================================================
-- 8. 验证生成的数据
-- ============================================================================

-- 显示各教室的使用记录统计
SELECT 
    c.room_no AS '教室编号',
    c.building AS '楼栋',
    c.type AS '类型',
    COUNT(*) AS '使用次数',
    SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) AS '总使用时长(分钟)',
    ROUND(SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100, 2) AS '使用率(%)',
    SUM(CASE WHEN cul.type = 'COURSE' THEN 1 ELSE 0 END) AS '课程次数',
    SUM(CASE WHEN cul.type = 'EXAM' THEN 1 ELSE 0 END) AS '考试次数',
    SUM(CASE WHEN cul.type = 'BOOKING' THEN 1 ELSE 0 END) AS '借用次数'
FROM classroom c
LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id
WHERE cul.start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.id, c.room_no, c.building, c.type
ORDER BY 
    CASE 
        WHEN ROUND(SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100, 2) >= 90 THEN 1
        WHEN ROUND(SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100, 2) >= 60 THEN 2
        WHEN ROUND(SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100, 2) >= 30 THEN 3
        ELSE 4
    END,
    c.room_no;

-- 显示使用率分布统计
SELECT 
    CASE 
        WHEN utilization_rate >= 90 THEN '高使用率(≥90%)'
        WHEN utilization_rate >= 60 THEN '正常使用率(60-90%)'
        WHEN utilization_rate >= 30 THEN '中等使用率(30-60%)'
        ELSE '低使用率(<30%)'
    END AS '使用率区间',
    COUNT(*) AS '教室数量'
FROM (
    SELECT 
        c.id,
        ROUND(SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100, 2) AS utilization_rate
    FROM classroom c
    LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id
    WHERE cul.start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
      AND c.deleted = FALSE
      AND c.status = 'AVAILABLE'
    GROUP BY c.id
) t
GROUP BY 
    CASE 
        WHEN utilization_rate >= 90 THEN '高使用率(≥90%)'
        WHEN utilization_rate >= 60 THEN '正常使用率(60-90%)'
        WHEN utilization_rate >= 30 THEN '中等使用率(30-60%)'
        ELSE '低使用率(<30%)'
    END
ORDER BY MIN(utilization_rate) DESC;

-- 显示楼栋使用率统计
SELECT 
    c.building AS '楼栋',
    COUNT(DISTINCT c.id) AS '教室数量',
    ROUND(AVG(
        SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100
    ), 2) AS '平均使用率(%)',
    SUM(CASE WHEN cul.type = 'COURSE' THEN 1 ELSE 0 END) AS '课程总次数',
    SUM(CASE WHEN cul.type = 'EXAM' THEN 1 ELSE 0 END) AS '考试总次数',
    SUM(CASE WHEN cul.type = 'BOOKING' THEN 1 ELSE 0 END) AS '借用总次数'
FROM classroom c
LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id
WHERE cul.start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND c.deleted = FALSE
  AND c.status = 'AVAILABLE'
GROUP BY c.building
ORDER BY c.building;

-- 显示类型使用率统计
SELECT 
    CASE c.type
        WHEN 'LECTURE' THEN '普通教室'
        WHEN 'MULTIMEDIA' THEN '多媒体教室'
        WHEN 'LAB' THEN '实验室'
        WHEN 'CONFERENCE' THEN '会议室'
    END AS '教室类型',
    COUNT(DISTINCT c.id) AS '教室数量',
    ROUND(AVG(
        SUM(TIMESTAMPDIFF(MINUTE, cul.start_time, cul.end_time)) / (30 * 12 * 60) * 100
    ), 2) AS '平均使用率(%)'
FROM classroom c
LEFT JOIN classroom_usage_log cul ON c.id = cul.classroom_id
WHERE cul.start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND c.deleted = FALSE
  AND c.status = 'AVAILABLE'
GROUP BY c.type
ORDER BY c.type;

-- ============================================================================
-- 完成
-- ============================================================================

COMMIT;

SELECT '✅ 教室使用率统计测试数据生成完成！' AS 状态;
SELECT CONCAT('📊 共生成 ', COUNT(*), ' 条使用记录') AS 统计 
FROM classroom_usage_log 
WHERE start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY);

