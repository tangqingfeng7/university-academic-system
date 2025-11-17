-- ============================================================================
-- 教学评价模块测试数据脚本
-- Description: 为评价模块创建测试数据
-- Author: Academic System Team
-- Date: 2025-01-09
-- 依赖: 需要先执行 V10__create_evaluation_tables.sql 和基础测试数据
-- ============================================================================

USE academic_system;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================================
-- 1. 清空现有评价数据（仅用于开发测试环境）
-- ============================================================================
-- 注意：生产环境请注释掉以下DELETE语句
DELETE FROM teacher_evaluation;
DELETE FROM course_evaluation;
DELETE FROM evaluation_period;

-- 重置自增ID
ALTER TABLE evaluation_period AUTO_INCREMENT = 1;
ALTER TABLE course_evaluation AUTO_INCREMENT = 1;
ALTER TABLE teacher_evaluation AUTO_INCREMENT = 1;

-- ============================================================================
-- 2. 插入评价周期测试数据
-- ============================================================================

-- 假设数据库中已有学期数据（semester表）
-- 这里创建几个评价周期，覆盖不同状态的场景

-- 2024-2025秋季学期评价周期（进行中）
INSERT INTO evaluation_period (semester_id, start_time, end_time, description, is_active, created_at) 
SELECT 
    id, 
    DATE_ADD(end_date, INTERVAL -30 DAY),  -- 学期结束前30天开始
    DATE_ADD(end_date, INTERVAL -10 DAY),  -- 学期结束前10天结束
    '2024-2025秋季学期教学质量评价',
    TRUE,
    NOW()
FROM semester 
WHERE academic_year = '2024-2025' AND semester_type = 2
LIMIT 1;

-- 2023-2024春季学期评价周期（已结束）
INSERT INTO evaluation_period (semester_id, start_time, end_time, description, is_active, created_at) 
SELECT 
    id, 
    DATE_ADD(end_date, INTERVAL -25 DAY),
    DATE_ADD(end_date, INTERVAL -5 DAY),
    '2023-2024春季学期教学质量评价',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 6 MONTH)
FROM semester 
WHERE academic_year = '2023-2024' AND semester_type = 1
LIMIT 1;

-- 2024-2025春季学期评价周期（未开始）
INSERT INTO evaluation_period (semester_id, start_time, end_time, description, is_active, created_at) 
SELECT 
    id, 
    DATE_ADD(NOW(), INTERVAL 30 DAY),  -- 30天后开始
    DATE_ADD(NOW(), INTERVAL 50 DAY),  -- 50天后结束
    '2024-2025春季学期教学质量评价',
    FALSE,
    NOW()
FROM semester 
WHERE academic_year = '2024-2025' AND semester_type = 1
LIMIT 1;

-- ============================================================================
-- 3. 插入课程评价测试数据
-- ============================================================================

-- 为2024-2025秋季学期的课程添加评价
-- 假设已有course_offering和course_selection数据

-- 高分评价（4-5星）
INSERT INTO course_evaluation (student_id, course_offering_id, rating, comment, anonymous, status, semester_id, flagged, created_at)
SELECT 
    cs.student_id,
    cs.offering_id,
    FLOOR(4 + RAND() * 2) AS rating,  -- 4-5星
    CASE 
        WHEN RAND() > 0.7 THEN '老师讲课非常好，课程内容丰富，收获很大！'
        WHEN RAND() > 0.4 THEN '课程设计合理，教学方法新颖，推荐大家选修。'
        ELSE '教师认真负责，课堂氛围活跃，学到了很多实用知识。'
    END AS comment,
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    co.semester_id,
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.3  -- 70%的学生参与评价
LIMIT 50;

-- 中等评价（3星）
INSERT INTO course_evaluation (student_id, course_offering_id, rating, comment, anonymous, status, semester_id, flagged, created_at)
SELECT 
    cs.student_id,
    cs.offering_id,
    3 AS rating,
    CASE 
        WHEN RAND() > 0.5 THEN '课程还可以，但有些内容可以再改进。'
        ELSE '教学质量一般，希望能增加更多实践内容。'
    END AS comment,
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    co.semester_id,
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.7  -- 30%给中等评价
  AND NOT EXISTS (
      SELECT 1 FROM course_evaluation ce 
      WHERE ce.student_id = cs.student_id 
        AND ce.course_offering_id = cs.offering_id
  )
LIMIT 15;

-- 低分评价（1-2星，少量）
INSERT INTO course_evaluation (student_id, course_offering_id, rating, comment, anonymous, status, semester_id, flagged, created_at)
SELECT 
    cs.student_id,
    cs.offering_id,
    FLOOR(1 + RAND() * 2) AS rating,  -- 1-2星
    '课程内容需要改进，教学方式有待提高。',
    TRUE,  -- 低分评价通常匿名
    'SUBMITTED',
    co.semester_id,
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.9  -- 10%给低分
  AND NOT EXISTS (
      SELECT 1 FROM course_evaluation ce 
      WHERE ce.student_id = cs.student_id 
        AND ce.course_offering_id = cs.offering_id
  )
LIMIT 5;

-- 包含敏感词的评价（已标记）
INSERT INTO course_evaluation (student_id, course_offering_id, rating, comment, anonymous, status, semester_id, flagged, moderation_note, created_at)
SELECT 
    cs.student_id,
    cs.offering_id,
    2 AS rating,
    '课程内容垃圾，教学质量差。',  -- 包含敏感词
    TRUE,
    'SUBMITTED',
    co.semester_id,
    TRUE,
    '检测到敏感词: 垃圾',
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.98  -- 极少数
  AND NOT EXISTS (
      SELECT 1 FROM course_evaluation ce 
      WHERE ce.student_id = cs.student_id 
        AND ce.course_offering_id = cs.offering_id
  )
LIMIT 2;

-- ============================================================================
-- 4. 插入教师评价测试数据
-- ============================================================================

-- 高分评价（4-5分）
INSERT INTO teacher_evaluation (student_id, teacher_id, course_offering_id, teaching_rating, attitude_rating, content_rating, comment, anonymous, status, flagged, created_at)
SELECT 
    cs.student_id,
    co.teacher_id,
    cs.offering_id,
    FLOOR(4 + RAND() * 2) AS teaching_rating,
    FLOOR(4 + RAND() * 2) AS attitude_rating,
    FLOOR(4 + RAND() * 2) AS content_rating,
    CASE 
        WHEN RAND() > 0.7 THEN '老师教学水平高，态度认真，课程内容充实。'
        WHEN RAND() > 0.4 THEN '非常负责任的老师，讲解清晰，课堂互动好。'
        ELSE '教师专业素养高，教学方法得当，深受学生欢迎。'
    END AS comment,
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.3  -- 70%的学生参与评价
LIMIT 50;

-- 中等评价（3分）
INSERT INTO teacher_evaluation (student_id, teacher_id, course_offering_id, teaching_rating, attitude_rating, content_rating, comment, anonymous, status, flagged, created_at)
SELECT 
    cs.student_id,
    co.teacher_id,
    cs.offering_id,
    3 AS teaching_rating,
    3 AS attitude_rating,
    3 AS content_rating,
    '教学表现一般，有改进空间。',
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.7  -- 30%给中等评价
  AND NOT EXISTS (
      SELECT 1 FROM teacher_evaluation te 
      WHERE te.student_id = cs.student_id 
        AND te.teacher_id = co.teacher_id
        AND te.course_offering_id = cs.offering_id
  )
LIMIT 15;

-- 低分评价（1-2分，少量）
INSERT INTO teacher_evaluation (student_id, teacher_id, course_offering_id, teaching_rating, attitude_rating, content_rating, comment, anonymous, status, flagged, created_at)
SELECT 
    cs.student_id,
    co.teacher_id,
    cs.offering_id,
    FLOOR(1 + RAND() * 2) AS teaching_rating,
    FLOOR(1 + RAND() * 2) AS attitude_rating,
    FLOOR(1 + RAND() * 2) AS content_rating,
    '教学方法需要改进，课堂效果不佳。',
    TRUE,  -- 低分评价通常匿名
    'SUBMITTED',
    FALSE,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2024-2025' 
  AND s.semester_type = 2
  AND RAND() > 0.9  -- 10%给低分
  AND NOT EXISTS (
      SELECT 1 FROM teacher_evaluation te 
      WHERE te.student_id = cs.student_id 
        AND te.teacher_id = co.teacher_id
        AND te.course_offering_id = cs.offering_id
  )
LIMIT 5;

-- ============================================================================
-- 5. 为历史学期添加一些评价数据（用于趋势分析）
-- ============================================================================

-- 2023-2024春季学期的历史评价数据
INSERT INTO course_evaluation (student_id, course_offering_id, rating, comment, anonymous, status, semester_id, flagged, created_at)
SELECT 
    cs.student_id,
    cs.offering_id,
    FLOOR(3 + RAND() * 3) AS rating,  -- 3-5星
    '往期课程评价',
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    co.semester_id,
    FALSE,
    DATE_SUB(NOW(), INTERVAL 6 MONTH)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2023-2024' 
  AND s.semester_type = 1
  AND RAND() > 0.5  -- 50%参与率
LIMIT 30;

INSERT INTO teacher_evaluation (student_id, teacher_id, course_offering_id, teaching_rating, attitude_rating, content_rating, comment, anonymous, status, flagged, created_at)
SELECT 
    cs.student_id,
    co.teacher_id,
    cs.offering_id,
    FLOOR(3 + RAND() * 3) AS teaching_rating,
    FLOOR(3 + RAND() * 3) AS attitude_rating,
    FLOOR(3 + RAND() * 3) AS content_rating,
    '往期教师评价',
    CASE WHEN RAND() > 0.3 THEN TRUE ELSE FALSE END AS anonymous,
    'SUBMITTED',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 6 MONTH)
FROM course_selection cs
INNER JOIN course_offering co ON cs.offering_id = co.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE cs.status = 'SELECTED'
  AND s.academic_year = '2023-2024' 
  AND s.semester_type = 1
  AND RAND() > 0.5  -- 50%参与率
LIMIT 30;

-- ============================================================================
-- 6. 验证数据插入结果
-- ============================================================================

-- 查看评价周期数量
SELECT '评价周期数量:' AS info, COUNT(*) AS count FROM evaluation_period;

-- 查看课程评价数量
SELECT '课程评价数量:' AS info, COUNT(*) AS count FROM course_evaluation;

-- 查看教师评价数量
SELECT '教师评价数量:' AS info, COUNT(*) AS count FROM teacher_evaluation;

-- 查看评价状态分布
SELECT '评价状态分布:' AS info, status, COUNT(*) AS count 
FROM course_evaluation 
GROUP BY status;

-- 查看课程评分分布
SELECT '课程评分分布:' AS info, rating, COUNT(*) AS count 
FROM course_evaluation 
GROUP BY rating 
ORDER BY rating DESC;

-- 查看被标记的评价数量
SELECT '被标记评价数量:' AS info, COUNT(*) AS count 
FROM course_evaluation 
WHERE flagged = TRUE;

-- 查看匿名评价比例
SELECT '匿名评价比例:' AS info, 
    CONCAT(ROUND(SUM(CASE WHEN anonymous = TRUE THEN 1 ELSE 0 END) / COUNT(*) * 100, 2), '%') AS rate
FROM course_evaluation;

-- 查看参与率最高的课程
SELECT '参与率最高的课程TOP 5:' AS info;
SELECT 
    c.name AS course_name,
    t.name AS teacher_name,
    COUNT(ce.id) AS evaluation_count,
    AVG(ce.rating) AS avg_rating
FROM course_evaluation ce
INNER JOIN course_offering co ON ce.course_offering_id = co.id
INNER JOIN course c ON co.course_id = c.id
INNER JOIN teacher t ON co.teacher_id = t.id
GROUP BY c.name, t.name
ORDER BY evaluation_count DESC
LIMIT 5;

-- ============================================================================
-- 完成
-- ============================================================================

COMMIT;

-- 提示信息
SELECT '========================================' AS '';
SELECT '教学评价模块测试数据插入完成！' AS '';
SELECT '========================================' AS '';

