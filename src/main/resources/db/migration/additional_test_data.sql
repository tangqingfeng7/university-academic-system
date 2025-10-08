-- 补充测试数据脚本
-- 添加更多选课记录、成绩和通知

USE academic_system;

-- 1. 为学生添加选课记录 (为前50个学生添加选课)
-- 每个学生选5-8门课
INSERT IGNORE INTO course_selection (student_id, course_offering_id, selection_time, status, created_at, updated_at)
SELECT 
    s.id as student_id,
    co.id as course_offering_id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as selection_time,
    'SELECTED' as status,
    NOW(),
    NOW()
FROM student s
CROSS JOIN course_offering co
WHERE s.id <= 50
AND co.semester_id = 3  -- 当前学期
AND co.status = 'PUBLISHED'
AND RAND() < 0.3  -- 30%概率选课,每个学生大约选10门课
AND NOT EXISTS (
    SELECT 1 FROM course_selection cs2 
    WHERE cs2.student_id = s.id AND cs2.course_offering_id = co.id
)
LIMIT 400;

-- 更新course_offering的enrolled数量
UPDATE course_offering co
SET enrolled = (
    SELECT COUNT(*)
    FROM course_selection cs
    WHERE cs.course_offering_id = co.id
    AND cs.status = 'SELECTED'
)
WHERE co.semester_id = 3;

-- 2. 为历史学期(2023-2024秋季和春季)添加开课计划
-- 2023-2024 秋季学期 (学期ID=1) - 计算机课程
INSERT IGNORE INTO course_offering (semester_id, course_id, teacher_id, schedule, location, capacity, enrolled, status, created_at, updated_at) VALUES
(1, 1, 1, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A101', 120, 95, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 2, 2, '[{"day":2,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A102', 100, 85, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 3, 3, '[{"day":3,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A103', 80, 72, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 16, 7, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C101', 150, 142, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 18, 9, '[{"day":3,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C103', 120, 108, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 74, 25, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E101', 200, 195, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 76, 27, '[{"day":3,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 98, 'PUBLISHED', '2023-08-20', '2023-08-20'),
(1, 78, 29, '[{"day":1,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E201', 150, 145, 'PUBLISHED', '2023-08-20', '2023-08-20');

-- 2023-2024 春季学期 (学期ID=2)
INSERT IGNORE INTO course_offering (semester_id, course_id, teacher_id, schedule, location, capacity, enrolled, status, created_at, updated_at) VALUES
(2, 4, 4, '[{"day":4,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A104', 90, 82, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 5, 5, '[{"day":5,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A105', 100, 91, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 6, 6, '[{"day":1,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A106', 100, 88, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 17, 8, '[{"day":2,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C102', 150, 138, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 19, 10, '[{"day":4,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C104', 120, 112, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 75, 26, '[{"day":2,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E103', 200, 192, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 77, 27, '[{"day":4,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 96, 'PUBLISHED', '2024-02-10', '2024-02-10'),
(2, 79, 30, '[{"day":2,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E202', 150, 142, 'PUBLISHED', '2024-02-10', '2024-02-10');

-- 3. 为历史学期的开课添加选课记录 (前30个学生)
-- 2023-2024 秋季学期
INSERT IGNORE INTO course_selection (student_id, course_offering_id, selection_time, status, created_at, updated_at)
SELECT 
    s.id,
    co.id,
    DATE_SUB('2023-09-01', INTERVAL FLOOR(RAND() * 15) DAY),
    'COMPLETED',
    '2023-08-25',
    '2024-01-20'
FROM student s
CROSS JOIN course_offering co
WHERE s.id <= 30
AND co.semester_id = 1
AND RAND() < 0.8
LIMIT 180;

-- 2023-2024 春季学期  
INSERT IGNORE INTO course_selection (student_id, course_offering_id, selection_time, status, created_at, updated_at)
SELECT 
    s.id,
    co.id,
    DATE_SUB('2024-03-01', INTERVAL FLOOR(RAND() * 15) DAY),
    'COMPLETED',
    '2024-02-15',
    '2024-07-05'
FROM student s
CROSS JOIN course_offering co
WHERE s.id <= 30
AND co.semester_id = 2
AND RAND() < 0.8
LIMIT 180;

-- 4. 为历史学期的选课记录添加成绩
-- 为2023-2024秋季学期的选课添加成绩
INSERT IGNORE INTO grade (course_selection_id, regular_score, midterm_score, final_score, total_score, status, submitted_at, created_at, updated_at)
SELECT 
    cs.id,
    60 + FLOOR(RAND() * 40),  -- 平时成绩 60-100
    60 + FLOOR(RAND() * 40),  -- 期中成绩 60-100
    60 + FLOOR(RAND() * 40),  -- 期末成绩 60-100
    NULL,  -- total_score will be calculated
    'PUBLISHED',
    '2024-01-20',
    '2024-01-15',
    '2024-01-20'
FROM course_selection cs
WHERE cs.course_offering_id IN (SELECT id FROM course_offering WHERE semester_id = 1)
AND cs.status = 'COMPLETED';

-- 计算总评成绩 (平时30% + 期中30% + 期末40%)
UPDATE grade g
SET total_score = ROUND(g.regular_score * 0.3 + g.midterm_score * 0.3 + g.final_score * 0.4, 2)
WHERE g.total_score IS NULL
AND g.course_selection_id IN (
    SELECT cs.id FROM course_selection cs
    WHERE cs.course_offering_id IN (SELECT id FROM course_offering WHERE semester_id = 1)
);

-- 为2023-2024春季学期的选课添加成绩
INSERT IGNORE INTO grade (course_selection_id, regular_score, midterm_score, final_score, total_score, status, submitted_at, created_at, updated_at)
SELECT 
    cs.id,
    60 + FLOOR(RAND() * 40),
    60 + FLOOR(RAND() * 40),
    60 + FLOOR(RAND() * 40),
    NULL,
    'PUBLISHED',
    '2024-07-05',
    '2024-06-30',
    '2024-07-05'
FROM course_selection cs
WHERE cs.course_offering_id IN (SELECT id FROM course_offering WHERE semester_id = 2)
AND cs.status = 'COMPLETED';

-- 计算总评成绩
UPDATE grade g
SET total_score = ROUND(g.regular_score * 0.3 + g.midterm_score * 0.3 + g.final_score * 0.4, 2)
WHERE g.total_score IS NULL
AND g.course_selection_id IN (
    SELECT cs.id FROM course_selection cs
    WHERE cs.course_offering_id IN (SELECT id FROM course_offering WHERE semester_id = 2)
);

-- 5. 为当前学期的部分选课添加草稿成绩 (教师正在录入中)
INSERT IGNORE INTO grade (course_selection_id, regular_score, midterm_score, final_score, total_score, status, submitted_at, created_at, updated_at)
SELECT 
    cs.id,
    CASE WHEN RAND() > 0.3 THEN 60 + FLOOR(RAND() * 40) ELSE NULL END,  -- 70%有平时成绩
    CASE WHEN RAND() > 0.5 THEN 60 + FLOOR(RAND() * 40) ELSE NULL END,  -- 50%有期中成绩
    NULL,  -- 期末成绩还没考
    NULL,
    'DRAFT',
    NULL,
    NOW(),
    NOW()
FROM course_selection cs
WHERE cs.course_offering_id IN (SELECT id FROM course_offering WHERE semester_id = 3)
AND cs.status = 'SELECTED'
AND RAND() < 0.6  -- 60%的选课有成绩记录
LIMIT 150;

-- 6. 添加通知公告
INSERT INTO notification (title, content, type, publisher_id, target_role, publish_time, active, created_at) VALUES
-- 系统通知
('2024-2025学年秋季学期选课通知', '各位同学：\n\n2024-2025学年秋季学期选课工作即将开始，请注意以下事项：\n1. 选课时间：2024年8月15日-9月10日\n2. 请合理安排课程，注意时间冲突\n3. 注意先修课程要求\n4. 如有问题请咨询教务处\n\n祝学习进步！\n\n教务处\n2024年8月10日', 'SYSTEM', 1, 'ALL', '2024-08-10 09:00:00', TRUE, '2024-08-10 09:00:00'),

('关于2024-2025学年秋季学期课程安排的通知', '各位老师、同学：\n\n2024-2025学年秋季学期课程安排已经发布，请登录系统查看。如有疑问请及时联系教务处。\n\n教务处\n2024年8月15日', 'SYSTEM', 1, 'ALL', '2024-08-15 10:00:00', TRUE, '2024-08-15 10:00:00'),

('关于做好期中考试准备工作的通知', '各位老师：\n\n期中考试即将开始，请各位老师做好以下工作：\n1. 准备好考试试卷\n2. 提前通知学生考试时间和地点\n3. 按时提交成绩\n\n教务处\n2024年10月20日', 'SYSTEM', 1, 'TEACHER', DATE_SUB(NOW(), INTERVAL 5 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY)),

('图书馆闭馆通知', '因系统升级，图书馆将于本周六(10月15日)闭馆一天，请同学们提前做好学习安排。\n\n图书馆\n2024年10月12日', 'SYSTEM', 1, 'STUDENT', DATE_SUB(NOW(), INTERVAL 3 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 课程通知
('《程序设计基础》第一次作业通知', '各位同学：\n\n请在下周一前完成第一次作业，作业内容详见课程网站。\n\n张伟老师\n2024年9月20日', 'COURSE', 2, 'STUDENT', DATE_SUB(NOW(), INTERVAL 10 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY)),

('《数据结构》课程调课通知', '各位同学：\n\n由于教室调整，《数据结构》课程下周三的课将调至教学楼A201，请注意！\n\n李娜老师\n2024年10月5日', 'COURSE', 3, 'STUDENT', DATE_SUB(NOW(), INTERVAL 2 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),

('《高等数学》期中考试安排', '各位同学：\n\n期中考试安排如下：\n时间：11月10日(周五) 14:00-16:00\n地点：教学楼C101\n范围：第1-5章\n\n请提前做好复习准备！\n\n赵丽老师\n2024年10月25日', 'COURSE', 8, 'STUDENT', DATE_SUB(NOW(), INTERVAL 1 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 成绩通知
('2023-2024学年春季学期成绩已发布', '各位同学：\n\n2023-2024学年春季学期成绩已全部发布，请登录系统查看。如对成绩有疑问，请在一周内向任课教师提出。\n\n教务处\n2024年7月10日', 'GRADE', 1, 'STUDENT', '2024-07-10 15:00:00', FALSE, '2024-07-10 15:00:00'),

('关于公布《程序设计基础》期中成绩的通知', '各位同学：\n\n《程序设计基础》期中考试成绩已公布，请登录系统查看。\n\n张伟老师\n2024年10月30日', 'GRADE', 2, 'STUDENT', NOW(), TRUE, NOW());

-- 7. 添加更多的学生选课记录 (为ID 51-100的学生添加选课)
INSERT IGNORE INTO course_selection (student_id, course_offering_id, selection_time, status, created_at, updated_at)
SELECT 
    s.id as student_id,
    co.id as course_offering_id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 25) DAY) as selection_time,
    'SELECTED' as status,
    NOW(),
    NOW()
FROM student s
CROSS JOIN course_offering co
WHERE s.id > 50 AND s.id <= 100
AND co.semester_id = 3
AND co.status = 'PUBLISHED'
AND RAND() < 0.28
LIMIT 400;

-- 更新course_offering的enrolled数量
UPDATE course_offering co
SET enrolled = (
    SELECT COUNT(*)
    FROM course_selection cs
    WHERE cs.course_offering_id = co.id
    AND cs.status = 'SELECTED'
)
WHERE co.semester_id = 3;

-- 8. 添加一些课程的先修关系
INSERT IGNORE INTO course_prerequisite (course_id, prerequisite_course_id) VALUES
-- 计算机课程的先修关系
(2, 1),   -- 数据结构需要先修程序设计基础
(3, 2),   -- 算法设计与分析需要先修数据结构
(4, 1),   -- 操作系统需要先修程序设计基础
(5, 1),   -- 计算机网络需要先修程序设计基础
(6, 2),   -- 数据库系统需要先修数据结构
(7, 1),   -- 软件工程需要先修程序设计基础
(8, 2),   -- 编译原理需要先修数据结构
(10, 2),  -- 机器学习需要先修数据结构
(10, 19), -- 机器学习需要先修概率论与数理统计

-- 数学课程的先修关系
(17, 16), -- 高等数学II需要先修高等数学I
(19, 16), -- 概率论与数理统计需要先修高等数学I
(22, 16), -- 复变函数需要先修高等数学I
(23, 16), -- 数值分析需要先修高等数学I

-- 物理课程的先修关系
(27, 26), -- 大学物理II需要先修大学物理I
(28, 27), -- 量子力学需要先修大学物理II

-- 化学课程的先修关系
(32, 31), -- 有机化学需要先修无机化学
(33, 32), -- 物理化学需要先修有机化学

-- 生物课程的先修关系
(42, 41), -- 细胞生物学需要先修普通生物学
(44, 42); -- 分子生物学需要先修细胞生物学

-- 提交所有更改
COMMIT;

-- 统计信息
SELECT '=== 数据统计 ===' as title;
SELECT '选课记录总数：', COUNT(*) FROM course_selection;
SELECT '成绩记录总数：', COUNT(*) FROM grade;
SELECT '已发布成绩数：', COUNT(*) FROM grade WHERE status = 'PUBLISHED';
SELECT '草稿成绩数：', COUNT(*) FROM grade WHERE status = 'DRAFT';
SELECT '通知公告数：', COUNT(*) FROM notification;
SELECT '先修课程关系数：', COUNT(*) FROM course_prerequisite;
SELECT '当前学期开课数：', COUNT(*) FROM course_offering WHERE semester_id = 3;
SELECT '当前学期选课数：', COUNT(*) FROM course_selection cs 
    JOIN course_offering co ON cs.course_offering_id = co.id 
    WHERE co.semester_id = 3;

SELECT '✓ 补充测试数据插入完成！' as status;

