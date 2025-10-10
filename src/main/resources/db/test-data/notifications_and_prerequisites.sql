-- 添加通知公告和先修课程关系
USE academic_system;

-- 添加通知公告
INSERT IGNORE INTO notification (title, content, type, publisher_id, target_role, publish_time, active, created_at) VALUES
('关于2024-2025学年秋季学期课程安排的通知', '各位老师、同学：\n\n2024-2025学年秋季学期课程安排已经发布，请登录系统查看。如有疑问请及时联系教务处。\n\n教务处\n2024年8月15日', 'SYSTEM', 1, 'ALL', '2024-08-15 10:00:00', TRUE, '2024-08-15 10:00:00'),

('关于做好期中考试准备工作的通知', '各位老师：\n\n期中考试即将开始，请各位老师做好以下工作：\n1. 准备好考试试卷\n2. 提前通知学生考试时间和地点\n3. 按时提交成绩\n\n教务处', 'SYSTEM', 1, 'TEACHER', DATE_SUB(NOW(), INTERVAL 5 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY)),

('图书馆闭馆通知', '因系统升级，图书馆将于本周六闭馆一天，请同学们提前做好学习安排。\n\n图书馆', 'SYSTEM', 1, 'STUDENT', DATE_SUB(NOW(), INTERVAL 3 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY)),

('《程序设计基础》第一次作业通知', '各位同学：\n\n请在下周一前完成第一次作业，作业内容详见课程网站。\n\n张伟老师', 'COURSE', 2, 'STUDENT', DATE_SUB(NOW(), INTERVAL 10 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY)),

('《数据结构》课程调课通知', '各位同学：\n\n由于教室调整，《数据结构》课程下周三的课将调至教学楼A201，请注意！\n\n李娜老师', 'COURSE', 3, 'STUDENT', DATE_SUB(NOW(), INTERVAL 2 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),

('《高等数学》期中考试安排', '各位同学：\n\n期中考试安排如下：\n时间：11月10日(周五) 14:00-16:00\n地点：教学楼C101\n范围：第1-5章\n\n请提前做好复习准备！\n\n赵丽老师', 'COURSE', 8, 'STUDENT', DATE_SUB(NOW(), INTERVAL 1 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),

('关于公布《程序设计基础》期中成绩的通知', '各位同学：\n\n《程序设计基础》期中考试成绩已公布，请登录系统查看。\n\n张伟老师', 'GRADE', 2, 'STUDENT', NOW(), TRUE, NOW());

-- 添加一些课程的先修关系
INSERT IGNORE INTO course_prerequisite (course_id, prerequisite_course_id) VALUES
(2, 1),
(3, 2),
(4, 1),
(5, 1),
(6, 2),
(7, 1),
(8, 2),
(10, 2),
(10, 19),
(17, 16),
(19, 16),
(22, 16),
(23, 16),
(27, 26),
(28, 27),
(32, 31),
(33, 32),
(42, 41),
(44, 42);

COMMIT;

SELECT '✓ 通知和先修课程数据添加完成！' as status;
SELECT COUNT(*) as notification_count FROM notification;
SELECT COUNT(*) as prerequisite_count FROM course_prerequisite;

