-- 排课优化模块测试数据（修正版）
-- 包含：排课约束、教师偏好、排课方案

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 先删除可能存在的旧数据
DELETE FROM scheduling_solution WHERE id IN (1, 2, 3);
DELETE FROM teacher_preference WHERE id IN (1, 2, 3, 4, 5);
DELETE FROM scheduling_constraint WHERE id BETWEEN 1 AND 10;

-- ==================== 排课约束数据 ====================

-- 硬约束（必须满足）
INSERT INTO scheduling_constraint (id, name, type, description, weight, active, created_at, updated_at) VALUES
(1, '教师时间不冲突', 'HARD', '同一教师不能在同一时间上多门课', 100, TRUE, NOW(), NOW()),
(2, '教室时间不冲突', 'HARD', '同一教室不能在同一时间安排多门课', 100, TRUE, NOW(), NOW()),
(3, '学生课程不冲突', 'HARD', '同一专业学生不能在同一时间上多门必修课', 100, TRUE, NOW(), NOW()),
(4, '教室容量限制', 'HARD', '课程人数不能超过教室容量', 100, TRUE, NOW(), NOW());

-- 软约束（尽量满足）
INSERT INTO scheduling_constraint (id, name, type, description, weight, active, created_at, updated_at) VALUES
(5, '教师偏好时间', 'SOFT', '优先安排教师偏好的时间段', 80, TRUE, NOW(), NOW()),
(6, '避免连续多节', 'SOFT', '避免教师连续上4节以上课程', 70, TRUE, NOW(), NOW()),
(7, '减少空闲时间', 'SOFT', '减少教师的空闲时段', 60, TRUE, NOW(), NOW()),
(8, '均衡分布', 'SOFT', '课程均匀分布在一周内', 50, TRUE, NOW(), NOW()),
(9, '上午优先', 'SOFT', '专业课优先安排在上午', 40, TRUE, NOW(), NOW()),
(10, '教室利用率', 'SOFT', '提高教室使用效率', 30, TRUE, NOW(), NOW());

-- ==================== 教师排课偏好数据 ====================

-- 为现有教师添加排课偏好
-- 教师1：张伟（teacher_no='T2024001'）
INSERT INTO teacher_preference (id, teacher_id, preferred_days, preferred_time_slots, max_daily_hours, max_weekly_hours, notes, created_at, updated_at)
SELECT 
    1,
    t.id,
    '1,2,3,4,5',  -- 周一到周五
    '1,2,3,4',    -- 上午时段（1-4节）
    6,            -- 每天最多6节课
    16,           -- 每周最多16节课
    '偏好上午时段授课，周三下午有科研活动',
    NOW(),
    NOW()
FROM teacher t 
WHERE t.teacher_no = 'T2024001'
LIMIT 1;

-- 教师2：李娜（teacher_no='T2024002'）
INSERT INTO teacher_preference (id, teacher_id, preferred_days, preferred_time_slots, max_daily_hours, max_weekly_hours, notes, created_at, updated_at)
SELECT 
    2,
    t.id,
    '1,2,3,4',    -- 周一到周四
    '3,4,5,6',    -- 下午时段（3-6节）
    4,            -- 每天最多4节课
    12,           -- 每周最多12节课
    '周五需要参加教研活动',
    NOW(),
    NOW()
FROM teacher t 
WHERE t.teacher_no = 'T2024002'
LIMIT 1;

-- 教师3：王强（teacher_no='T2024003'）
INSERT INTO teacher_preference (id, teacher_id, preferred_days, preferred_time_slots, max_daily_hours, max_weekly_hours, notes, created_at, updated_at)
SELECT 
    3,
    t.id,
    '2,3,4,5',    -- 周二到周五
    '1,2,5,6',    -- 上午和下午
    6,
    18,
    '时间相对灵活',
    NOW(),
    NOW()
FROM teacher t 
WHERE t.teacher_no = 'T2024003'
LIMIT 1;

-- 教师4：赵敏（teacher_no='T2024004'）
INSERT INTO teacher_preference (id, teacher_id, preferred_days, preferred_time_slots, max_daily_hours, max_weekly_hours, notes, created_at, updated_at)
SELECT 
    4,
    t.id,
    '1,3,5',      -- 周一、三、五
    '1,2,3,4',    -- 上午
    4,
    12,
    '只在周一、三、五上课',
    NOW(),
    NOW()
FROM teacher t 
WHERE t.teacher_no = 'T2024004'
LIMIT 1;

-- 教师5：刘洋（teacher_no='T2024005'）
INSERT INTO teacher_preference (id, teacher_id, preferred_days, preferred_time_slots, max_daily_hours, max_weekly_hours, notes, created_at, updated_at)
SELECT 
    5,
    t.id,
    '1,2,3,4,5',  -- 周一到周五
    '2,3,4,5,6,7', -- 避开第一节课
    8,
    20,
    '不喜欢第一节课，其他时间都可以',
    NOW(),
    NOW()
FROM teacher t 
WHERE t.teacher_no = 'T2024005'
LIMIT 1;

-- ==================== 排课方案数据 ====================

-- 为当前活跃学期创建排课方案
-- 注意：semester_type 为 TINYINT: 1-春季学期, 2-秋季学期
INSERT INTO scheduling_solution (id, semester_id, name, quality_score, conflict_count, status, generated_at, created_at, updated_at)
SELECT 
    1,
    s.id,
    CONCAT(s.academic_year, IF(s.semester_type=1, '春季', '秋季'), '学期排课方案A'),
    NULL,  -- 未评估
    0,     -- 初始无冲突
    'DRAFT',  -- 草稿状态
    NOW(),
    NOW(),
    NOW()
FROM semester s
WHERE s.active = TRUE
ORDER BY s.id DESC
LIMIT 1;

-- 创建第二个方案用于对比
INSERT INTO scheduling_solution (id, semester_id, name, quality_score, conflict_count, status, generated_at, created_at, updated_at)
SELECT 
    2,
    s.id,
    CONCAT(s.academic_year, IF(s.semester_type=1, '春季', '秋季'), '学期排课方案B'),
    NULL,
    0,
    'DRAFT',
    NOW(),
    NOW(),
    NOW()
FROM semester s
WHERE s.active = TRUE
ORDER BY s.id DESC
LIMIT 1;

-- 创建一个已完成的方案（带有质量评分）
INSERT INTO scheduling_solution (id, semester_id, name, quality_score, conflict_count, status, generated_at, created_at, updated_at)
SELECT 
    3,
    s.id,
    CONCAT(s.academic_year, IF(s.semester_type=1, '春季', '秋季'), '学期优化方案'),
    85.5,  -- 质量分数
    2,     -- 2个冲突
    'COMPLETED',  -- 已完成
    NOW(),
    NOW(),
    NOW()
FROM semester s
WHERE s.active = TRUE
ORDER BY s.id DESC
LIMIT 1;

-- ==================== 说明 ====================
-- 约束说明：
-- 1. 硬约束（weight=100）：必须满足，违反会导致排课失败
-- 2. 软约束（weight<100）：尽量满足，权重越高优先级越高
--
-- 教师偏好说明：
-- preferred_days: 1=周一, 2=周二, 3=周三, 4=周四, 5=周五
-- preferred_time_slots: 1-8 表示第1-8节课
-- 一般安排：1-2节(8:00-10:00), 3-4节(10:00-12:00), 5-6节(14:00-16:00), 7-8节(16:00-18:00)
--
-- 排课方案状态：
-- DRAFT: 草稿，可以修改
-- OPTIMIZING: 正在优化中
-- COMPLETED: 已完成，可以预览
-- APPLIED: 已应用到课程表

