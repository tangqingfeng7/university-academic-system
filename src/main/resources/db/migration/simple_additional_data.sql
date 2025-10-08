-- 简化版补充测试数据脚本
USE academic_system;

-- 1. 为更多学生添加当前学期选课记录 (ID 51-120)
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
WHERE s.id >= 51 AND s.id <= 120
AND co.semester_id = 3
AND co.status = 'PUBLISHED'
AND RAND() < 0.25
AND NOT EXISTS (
    SELECT 1 FROM course_selection cs2 
    WHERE cs2.student_id = s.id AND cs2.course_offering_id = co.id
)
LIMIT 500;

-- 2. 更新course_offering的enrolled数量
UPDATE course_offering co
SET enrolled = (
    SELECT COUNT(*)
    FROM course_selection cs
    WHERE cs.course_offering_id = co.id
    AND cs.status = 'SELECTED'
)
WHERE co.semester_id = 3;

-- 3. 添加通知公告
INSERT IGNORE INTO notification (title, content, type, publisher_id, target_role, publish_time, active, created_at) VALUES
('2024-2025学年秋季学期选课通知', '各位同学：\n\n2024-2025学年秋季学期选课工作即将开始，请注意以下事项：\n1. 选课时间：2024年8月15日-9月10日\n2. 请合理安排课程，注意时间冲突\n3. 注意先修课程要求\n4. 如有问题请咨询教务处\n\n祝学习进步！\n\n教务处\n2024年8月10日', 'SYSTEM', 1, 'ALL', '2024-08-10 09:00:00', TRUE, '2024-08-10 09:00:00'),

('关于2024-2025学年秋季学期课程安排的通知', '各位老师、同学：\n\n2024-2025学年秋季学期课程安排已经发布，请登录系统查看。如有疑问请及时联系教务处。\n\n教务处\n2024年8月15日', 'SYSTEM', 1, 'ALL', '2024-08-15 10:00:00', TRUE, '2024-08-15 10:00:00'),

('关于做好期中考试准备工作的通知', '各位老师：\n\n期中考试即将开始，请各位老师做好以下工作：\n1. 准备好考试试卷\n2. 提前通知学生考试时间和地点\n3. 按时提交成绩\n\n教务处', 'SYSTEM', 1, 'TEACHER', DATE_SUB(NOW(), INTERVAL 5 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY)),

('图书馆闭馆通知', '因系统升级，图书馆将于本周六闭馆一天，请同学们提前做好学习安排。\n\n图书馆', 'SYSTEM', 1, 'STUDENT', DATE_SUB(NOW(), INTERVAL 3 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY)),

('《程序设计基础》第一次作业通知', '各位同学：\n\n请在下周一前完成第一次作业，作业内容详见课程网站。\n\n张伟老师', 'COURSE', 2, 'STUDENT', DATE_SUB(NOW(), INTERVAL 10 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY)),

('《数据结构》课程调课通知', '各位同学：\n\n由于教室调整，《数据结构》课程下周三的课将调至教学楼A201，请注意！\n\n李娜老师', 'COURSE', 3, 'STUDENT', DATE_SUB(NOW(), INTERVAL 2 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),

('《高等数学》期中考试安排', '各位同学：\n\n期中考试安排如下：\n时间：11月10日(周五) 14:00-16:00\n地点：教学楼C101\n范围：第1-5章\n\n请提前做好复习准备！\n\n赵丽老师', 'COURSE', 8, 'STUDENT', DATE_SUB(NOW(), INTERVAL 1 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),

('关于公布《程序设计基础》期中成绩的通知', '各位同学：\n\n《程序设计基础》期中考试成绩已公布，请登录系统查看。\n\n张伟老师', 'GRADE', 2, 'STUDENT', NOW(), TRUE, NOW());

-- 4. 添加一些课程的先修关系
INSERT IGNORE INTO course_prerequisite (course_id, prerequisite_course_id) VALUES
(2, 1),   -- 数据结构需要先修程序设计基础
(3, 2),   -- 算法设计与分析需要先修数据结构
(4, 1),   -- 操作系统需要先修程序设计基础
(5, 1),   -- 计算机网络需要先修程序设计基础
(6, 2),   -- 数据库系统需要先修数据结构
(7, 1),   -- 软件工程需要先修程序设计基础
(8, 2),   -- 编译原理需要先修数据结构
(10, 2),  -- 机器学习需要先修数据结构
(10, 19), -- 机器学习需要先修概率论与数理统计
(17, 16), -- 高等数学II需要先修高等数学I
(19, 16), -- 概率论与数理统计需要先修高等数学I
(22, 16), -- 复变函数需要先修高等数学I
(23, 16), -- 数值分析需要先修高等数学I
(27, 26), -- 大学物理II需要先修大学物理I
(28, 27), -- 量子力学需要先修大学物理II
(32, 31), -- 有机化学需要先修无机化学
(33, 32), -- 物理化学需要先修有机化学
(42, 41), -- 细胞生物学需要先修普通生物学
(44, 42); -- 分子生物学需要先修细胞生物学

-- 提交所有更改
COMMIT;

-- 统计信息
SELECT '=== 数据统计 ===' as title;
SELECT '选课记录总数：' as label, COUNT(*) as count FROM course_selection;
SELECT '成绩记录总数：' as label, COUNT(*) as count FROM grade;
SELECT '通知公告数：' as label, COUNT(*) as count FROM notification;
SELECT '先修课程关系数：' as label, COUNT(*) as count FROM course_prerequisite;
SELECT '当前学期开课数：' as label, COUNT(*) as count FROM course_offering WHERE semester_id = 3;
SELECT '当前学期选课数：' as label, COUNT(*) as count FROM course_selection cs 
    JOIN course_offering co ON cs.course_offering_id = co.id 
    WHERE co.semester_id = 3;

SELECT '✓ 补充测试数据插入完成！' as status;

