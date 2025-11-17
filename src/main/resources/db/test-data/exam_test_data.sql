-- 考试模块测试数据脚本
-- 包含不同类型和状态的考试、考场、学生分配、监考安排
-- 覆盖正常场景、冲突场景、边界情况

USE academic_system;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ==================== 1. 考试数据 ====================
-- 使用实际的开课计划ID（从course_offering表查询得到）
-- 当前学期ID为3（2024-2025秋季）

-- 1.1 正常已发布的期末考试
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('程序设计基础期末考试', 'FINAL', 83, '2025-01-10 09:00:00', 120, 100, 'PUBLISHED', 
'考试范围：第1-12章\n考试题型：选择题30分、填空题20分、编程题50分\n允许携带：笔、橡皮、尺子\n不允许携带：计算器、手机、书籍', 
NOW(), NOW()),

('数据结构期末考试', 'FINAL', 84, '2025-01-12 14:00:00', 120, 100, 'PUBLISHED', 
'考试范围：数组、链表、栈、队列、树、图、排序、查找\n考试题型：选择题20分、简答题30分、算法设计50分\n闭卷考试', 
NOW(), NOW()),

('算法设计与分析期末考试', 'FINAL', 85, '2025-01-08 09:00:00', 150, 100, 'PUBLISHED', 
'考试范围：分治、贪心、动态规划、回溯、分支限界\n考试题型：选择题20分、算法分析30分、算法设计50分', 
NOW(), NOW()),

('高等数学期末考试', 'FINAL', 93, '2025-01-15 09:00:00', 120, 100, 'PUBLISHED', 
'考试范围：极限、导数、积分、微分方程\n允许携带：普通计算器（非编程型）', 
NOW(), NOW());

-- 1.2 期中考试（已结束）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('程序设计基础期中考试', 'MIDTERM', 83, '2024-11-08 14:00:00', 90, 100, 'FINISHED', 
'考试范围：第1-6章\n考试题型：选择题40分、编程题60分', 
DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),

('数据结构期中考试', 'MIDTERM', 84, '2024-11-10 09:00:00', 90, 100, 'FINISHED', 
'考试范围：数组、链表、栈、队列\n考试题型：选择题30分、简答题30分、编程题40分', 
DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),

('高等数学期中考试', 'MIDTERM', 93, '2024-11-05 14:00:00', 90, 100, 'FINISHED', 
'考试范围：极限、导数、微分\n允许携带：普通计算器', 
DATE_SUB(NOW(), INTERVAL 33 DAY), NOW());

-- 1.3 进行中的考试（测试自动状态更新）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('操作系统期中考试', 'MIDTERM', 86, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 120, 100, 'IN_PROGRESS', 
'考试范围：进程管理、内存管理\n考试题型：选择题40分、简答题60分', 
DATE_SUB(NOW(), INTERVAL 7 DAY), NOW());

-- 1.4 草稿状态的考试（未发布）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('计算机网络期末考试', 'FINAL', 87, '2025-01-20 09:00:00', 120, 100, 'DRAFT', 
'待定', NOW(), NOW()),

('数据库系统期末考试', 'FINAL', 88, '2025-01-18 14:00:00', 120, 100, 'DRAFT', 
'考试范围：SQL、关系代数、事务、索引\n考试题型：待定', NOW(), NOW());

-- 1.5 已取消的考试
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('软件工程期中考试', 'MIDTERM', 89, '2024-11-15 09:00:00', 90, 100, 'CANCELLED', 
'因课程调整，本次考试取消', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW());

-- 1.6 补考和重修考试
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('程序设计基础补考', 'MAKEUP', 83, '2025-02-28 09:00:00', 120, 100, 'PUBLISHED', 
'适用于期末考试不及格的学生\n考试范围：第1-12章（与期末考试相同）', 
NOW(), NOW()),

('数据结构重修考试', 'RETAKE', 84, '2025-03-05 14:00:00', 120, 100, 'PUBLISHED', 
'适用于重修学生\n考试范围：全部章节', 
NOW(), NOW());

-- 1.7 测试时间冲突场景（同一天同一时间段的不同考试）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('编译原理期末考试', 'FINAL', 90, '2025-01-10 09:00:00', 120, 100, 'PUBLISHED', 
'考试范围：词法分析、语法分析、语义分析\n考试题型：选择题30分、简答题40分、设计题30分', 
NOW(), NOW());

-- 1.8 边界情况：最短和最长考试时长
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('C++编程基础测验', 'MIDTERM', 91, '2024-12-20 10:00:00', 30, 50, 'PUBLISHED', 
'快速测验：基础语法和概念\n考试题型：选择题和填空题', 
NOW(), NOW()),

('人工智能综合考试', 'FINAL', 92, '2025-01-25 09:00:00', 180, 150, 'PUBLISHED', 
'考试范围：全部章节\n考试题型：理论题50分、编程题100分\n允许3小时完成', 
NOW(), NOW());

-- 获取插入的考试ID
SET @exam1 = (SELECT id FROM exam WHERE name = '程序设计基础期末考试' ORDER BY id DESC LIMIT 1);
SET @exam2 = (SELECT id FROM exam WHERE name = '数据结构期末考试' ORDER BY id DESC LIMIT 1);
SET @exam3 = (SELECT id FROM exam WHERE name = '算法设计与分析期末考试' ORDER BY id DESC LIMIT 1);
SET @exam4 = (SELECT id FROM exam WHERE name = '高等数学期末考试' ORDER BY id DESC LIMIT 1);
SET @exam5 = (SELECT id FROM exam WHERE name = '程序设计基础期中考试' ORDER BY id DESC LIMIT 1);
SET @exam6 = (SELECT id FROM exam WHERE name = '数据结构期中考试' ORDER BY id DESC LIMIT 1);
SET @exam7 = (SELECT id FROM exam WHERE name = '高等数学期中考试' ORDER BY id DESC LIMIT 1);
SET @exam8 = (SELECT id FROM exam WHERE name = '操作系统期中考试' ORDER BY id DESC LIMIT 1);
SET @exam12 = (SELECT id FROM exam WHERE name = '程序设计基础补考' ORDER BY id DESC LIMIT 1);
SET @exam13 = (SELECT id FROM exam WHERE name = '数据结构重修考试' ORDER BY id DESC LIMIT 1);
SET @exam14 = (SELECT id FROM exam WHERE name = '编译原理期末考试' ORDER BY id DESC LIMIT 1);
SET @exam15 = (SELECT id FROM exam WHERE name = 'C++编程基础测验' ORDER BY id DESC LIMIT 1);
SET @exam16 = (SELECT id FROM exam WHERE name = '人工智能综合考试' ORDER BY id DESC LIMIT 1);
SET @exam17 = (SELECT id FROM exam WHERE name = '计算机组成原理期末考试' ORDER BY id DESC LIMIT 1);
SET @exam18 = (SELECT id FROM exam WHERE name = '离散数学期末考试' ORDER BY id DESC LIMIT 1);

-- ==================== 2. 考场数据 ====================

-- 2.1 程序设计基础期末考试考场（多个考场，学生分布）
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam1, '考场1', '教学楼A101', 60, 0, NOW(), NOW()),
(@exam1, '考场2', '教学楼A102', 60, 0, NOW(), NOW()),
(@exam1, '考场3', '教学楼A103', 40, 0, NOW(), NOW());

-- 2.2 数据结构期末考试考场
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam2, '考场1', '教学楼B201', 50, 0, NOW(), NOW()),
(@exam2, '考场2', '教学楼B202', 50, 0, NOW(), NOW());

-- 2.3 算法设计与分析期末考试考场
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam3, '考场1', '教学楼C301', 40, 0, NOW(), NOW()),
(@exam3, '考场2', '教学楼C302', 40, 0, NOW(), NOW());

-- 2.4 高等数学期末考试考场（大考场）
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam4, '考场1', '大礼堂东', 100, 0, NOW(), NOW()),
(@exam4, '考场2', '大礼堂西', 100, 0, NOW(), NOW());

-- 2.5 期中考试考场（已结束）
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam5, '考场1', '教学楼A101', 60, 50, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(@exam6, '考场1', '教学楼B201', 50, 40, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
(@exam7, '考场1', '大礼堂东', 100, 80, DATE_SUB(NOW(), INTERVAL 33 DAY), NOW());

-- 2.6 进行中的考试考场
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam8, '考场1', '教学楼D401', 40, 35, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW());

-- 2.7 补考和重修考试考场（小考场）
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam12, '补考考场', '教学楼E101', 20, 0, NOW(), NOW()),
(@exam13, '重修考场', '教学楼E102', 15, 0, NOW(), NOW());

-- 2.8 时间冲突的考试考场
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam14, '考场1', '教学楼F201', 40, 0, NOW(), NOW());

-- 2.9 边界情况考场
INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam15, '小测验考场', '教学楼G101', 30, 0, NOW(), NOW()),
(@exam16, '大型考场', '体育馆', 200, 0, NOW(), NOW());


-- ==================== 3. 考场学生分配数据 ====================

-- 获取考场ID
SET @room1 = (SELECT id FROM exam_room WHERE exam_id = @exam1 AND room_name = '考场1' LIMIT 1);
SET @room2 = (SELECT id FROM exam_room WHERE exam_id = @exam1 AND room_name = '考场2' LIMIT 1);
SET @room3 = (SELECT id FROM exam_room WHERE exam_id = @exam1 AND room_name = '考场3' LIMIT 1);
SET @room4 = (SELECT id FROM exam_room WHERE exam_id = @exam2 AND room_name = '考场1' LIMIT 1);
SET @room5 = (SELECT id FROM exam_room WHERE exam_id = @exam2 AND room_name = '考场2' LIMIT 1);
SET @room6 = (SELECT id FROM exam_room WHERE exam_id = @exam3 AND room_name = '考场1' LIMIT 1);
SET @room7 = (SELECT id FROM exam_room WHERE exam_id = @exam3 AND room_name = '考场2' LIMIT 1);
SET @room8 = (SELECT id FROM exam_room WHERE exam_id = @exam4 AND room_name = '考场1' LIMIT 1);
SET @room9 = (SELECT id FROM exam_room WHERE exam_id = @exam4 AND room_name = '考场2' LIMIT 1);
SET @room10 = (SELECT id FROM exam_room WHERE exam_id = @exam5 AND room_name = '考场1' LIMIT 1);
SET @room11 = (SELECT id FROM exam_room WHERE exam_id = @exam6 AND room_name = '考场1' LIMIT 1);
SET @room12 = (SELECT id FROM exam_room WHERE exam_id = @exam7 AND room_name = '考场1' LIMIT 1);
SET @room13 = (SELECT id FROM exam_room WHERE exam_id = @exam8 AND room_name = '考场1' LIMIT 1);
SET @room14 = (SELECT id FROM exam_room WHERE exam_id = @exam12 AND room_name = '补考考场' LIMIT 1);
SET @room15 = (SELECT id FROM exam_room WHERE exam_id = @exam13 AND room_name = '重修考场' LIMIT 1);
SET @room16 = (SELECT id FROM exam_room WHERE exam_id = @exam14 AND room_name = '考场1' LIMIT 1);
SET @room17 = (SELECT id FROM exam_room WHERE exam_id = @exam17 AND room_name = '满容量考场' LIMIT 1);
SET @room18 = (SELECT id FROM exam_room WHERE exam_id = @exam18 AND room_name = '空考场' LIMIT 1);

-- 3.1 程序设计基础期末考试学生分配（假设有选课记录）
-- 考场1：学号1-60的学生
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room1, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE s.id BETWEEN 1 AND 60
  AND EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 83 AND cs.status = 'SELECTED')
LIMIT 60;

-- 更新考场1的assigned_count
UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room1) WHERE id = @room1;

-- 考场2：学号61-120的学生
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room2, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE s.id BETWEEN 61 AND 120
  AND EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 83 AND cs.status = 'SELECTED')
LIMIT 60;

-- 更新考场2的assigned_count
UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room2) WHERE id = @room2;

-- 3.2 数据结构期末考试学生分配
-- 考场1：前50名学生
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room4, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 84 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 50;

-- 更新考场assigned_count
UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room4) WHERE id = @room4;

-- 考场2：后续学生
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room5, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 84 AND cs.status = 'SELECTED')
  AND s.id NOT IN (SELECT student_id FROM exam_room_student WHERE exam_room_id = @room4)
ORDER BY s.id
LIMIT 50;

-- 更新考场assigned_count
UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room5) WHERE id = @room5;

-- 3.3 算法设计与分析期末考试学生分配
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room6, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 85 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 40;

UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room6) WHERE id = @room6;

INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room7, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 85 AND cs.status = 'SELECTED')
  AND s.id NOT IN (SELECT student_id FROM exam_room_student WHERE exam_room_id = @room6)
ORDER BY s.id
LIMIT 40;

UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room7) WHERE id = @room7;

-- 3.4 高等数学期末考试学生分配（大量学生）
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room8, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 93 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 100;

UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room8) WHERE id = @room8;

INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room9, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 93 AND cs.status = 'SELECTED')
  AND s.id NOT IN (SELECT student_id FROM exam_room_student WHERE exam_room_id = @room8)
ORDER BY s.id
LIMIT 100;

UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room9) WHERE id = @room9;

-- 3.5 期中考试学生分配（已结束的考试）
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room10, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 83 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 50;

INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room11, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 84 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 40;

INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room12, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 93 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 80;

-- 3.6 进行中的考试学生分配
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room13, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 86 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 35;

UPDATE exam_room SET assigned_count = (SELECT COUNT(*) FROM exam_room_student WHERE exam_room_id = @room13) WHERE id = @room13;


-- ==================== 4. 监考安排数据 ====================

-- 4.1 程序设计基础期末考试监考（多个考场）
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
-- 考场1：主监考和副监考
(@room1, 2, 'CHIEF', NOW(), NOW()),      -- 李娜老师主监考
(@room1, 3, 'ASSISTANT', NOW(), NOW()),  -- 王强老师副监考
-- 考场2：主监考和副监考
(@room2, 4, 'CHIEF', NOW(), NOW()),      -- 赵敏老师主监考
(@room2, 5, 'ASSISTANT', NOW(), NOW()),  -- 刘洋老师副监考
-- 考场3：主监考
(@room3, 6, 'CHIEF', NOW(), NOW());      -- 陈静老师主监考

-- 4.2 数据结构期末考试监考
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
-- 考场1
(@room4, 7, 'CHIEF', NOW(), NOW()),      -- 杨帆老师主监考
(@room4, 8, 'ASSISTANT', NOW(), NOW()),  -- 吴彤老师副监考
-- 考场2
(@room5, 9, 'CHIEF', NOW(), NOW()),      -- 郑宇老师主监考
(@room5, 10, 'ASSISTANT', NOW(), NOW()); -- 孙丽老师副监考

-- 4.3 算法设计与分析期末考试监考
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room6, 11, 'CHIEF', NOW(), NOW()),     -- 周文老师主监考
(@room6, 12, 'ASSISTANT', NOW(), NOW()),
(@room7, 13, 'CHIEF', NOW(), NOW()),
(@room7, 14, 'ASSISTANT', NOW(), NOW());

-- 4.4 高等数学期末考试监考（大考场，多名监考）
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
-- 考场1（大礼堂东）
(@room8, 15, 'CHIEF', NOW(), NOW()),
(@room8, 16, 'ASSISTANT', NOW(), NOW()),
(@room8, 17, 'ASSISTANT', NOW(), NOW()),  -- 额外副监考
-- 考场2（大礼堂西）
(@room9, 18, 'CHIEF', NOW(), NOW()),
(@room9, 19, 'ASSISTANT', NOW(), NOW()),
(@room9, 20, 'ASSISTANT', NOW(), NOW());  -- 额外副监考

-- 4.5 期中考试监考（已结束）
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room10, 2, 'CHIEF', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(@room10, 3, 'ASSISTANT', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(@room11, 7, 'CHIEF', DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
(@room11, 8, 'ASSISTANT', DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
(@room12, 15, 'CHIEF', DATE_SUB(NOW(), INTERVAL 33 DAY), NOW()),
(@room12, 16, 'ASSISTANT', DATE_SUB(NOW(), INTERVAL 33 DAY), NOW());

-- 4.6 进行中的考试监考
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room13, 21, 'CHIEF', DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
(@room13, 22, 'ASSISTANT', DATE_SUB(NOW(), INTERVAL 7 DAY), NOW());

-- 4.7 补考和重修考试监考
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room14, 23, 'CHIEF', NOW(), NOW()),    -- 补考监考
(@room15, 24, 'CHIEF', NOW(), NOW());    -- 重修考试监考

-- 4.8 时间冲突场景：同一教师在相近时间段监考（测试冲突检测）
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room16, 25, 'CHIEF', NOW(), NOW());

-- 4.9 边界情况：教师监考负担测试
-- 教师26在多个考试中监考（测试监考统计功能）
INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room1, 26, 'ASSISTANT', NOW(), NOW()),  -- 额外副监考
(@room4, 26, 'ASSISTANT', NOW(), NOW()),  -- 额外副监考
(@room6, 26, 'ASSISTANT', NOW(), NOW());  -- 额外副监考


-- ==================== 5. 额外测试场景数据 ====================

-- 5.1 满容量考场（测试容量限制）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('计算机组成原理期末考试', 'FINAL', 96, '2025-01-22 09:00:00', 120, 100, 'PUBLISHED', 
'满容量考场测试', NOW(), NOW());

SET @exam17 = LAST_INSERT_ID();

INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam17, '满容量考场', '教学楼H101', 30, 30, NOW(), NOW());

SET @room17 = LAST_INSERT_ID();

-- 分配30个学生（达到容量上限）
INSERT INTO exam_room_student (exam_room_id, student_id, seat_number, created_at, updated_at)
SELECT @room17, s.id, CONCAT(CHAR(65 + FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 30)), 
       LPAD(((ROW_NUMBER() OVER (ORDER BY s.id) - 1) % 30) + 1, 2, '0')), NOW(), NOW()
FROM student s
WHERE EXISTS (SELECT 1 FROM course_selection cs WHERE cs.student_id = s.id AND cs.course_offering_id = 96 AND cs.status = 'SELECTED')
ORDER BY s.id
LIMIT 30;

INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room17, 27, 'CHIEF', NOW(), NOW());

-- 5.2 空考场（测试边界情况）
INSERT INTO exam (name, type, course_offering_id, exam_time, duration, total_score, status, description, created_at, updated_at) VALUES
('离散数学期末考试', 'FINAL', 97, '2025-01-28 14:00:00', 120, 100, 'PUBLISHED', 
'空考场测试', NOW(), NOW());

SET @exam18 = LAST_INSERT_ID();

INSERT INTO exam_room (exam_id, room_name, location, capacity, assigned_count, created_at, updated_at) VALUES
(@exam18, '空考场', '教学楼I101', 50, 0, NOW(), NOW());

SET @room18 = LAST_INSERT_ID();

INSERT INTO exam_invigilator (exam_room_id, teacher_id, type, created_at, updated_at) VALUES
(@room18, 28, 'CHIEF', NOW(), NOW());


-- ==================== 6. 更新统计信息 ====================

-- 提交所有更改
COMMIT;

-- 显示统计信息
SELECT '=== 考试测试数据统计 ===' as title;
SELECT '考试总数：' as label, COUNT(*) as count FROM exam;
SELECT '  - 草稿状态：' as label, COUNT(*) as count FROM exam WHERE status = 'DRAFT';
SELECT '  - 已发布：' as label, COUNT(*) as count FROM exam WHERE status = 'PUBLISHED';
SELECT '  - 进行中：' as label, COUNT(*) as count FROM exam WHERE status = 'IN_PROGRESS';
SELECT '  - 已结束：' as label, COUNT(*) as count FROM exam WHERE status = 'FINISHED';
SELECT '  - 已取消：' as label, COUNT(*) as count FROM exam WHERE status = 'CANCELLED';
SELECT '考试类型统计：' as label, '' as count;
SELECT '  - 期中考试：' as label, COUNT(*) as count FROM exam WHERE type = 'MIDTERM';
SELECT '  - 期末考试：' as label, COUNT(*) as count FROM exam WHERE type = 'FINAL';
SELECT '  - 补考：' as label, COUNT(*) as count FROM exam WHERE type = 'MAKEUP';
SELECT '  - 重修考试：' as label, COUNT(*) as count FROM exam WHERE type = 'RETAKE';
SELECT '考场总数：' as label, COUNT(*) as count FROM exam_room;
SELECT '考场学生分配总数：' as label, COUNT(*) as count FROM exam_room_student;
SELECT '监考安排总数：' as label, COUNT(*) as count FROM exam_invigilator;
SELECT '场景覆盖：' as label, '' as count;
SELECT '  ✓ 正常已发布考试' as scenario;
SELECT '  ✓ 期中/期末/补考/重修考试' as scenario;
SELECT '  ✓ 草稿/已发布/进行中/已结束/已取消状态' as scenario;
SELECT '  ✓ 多考场分配' as scenario;
SELECT '  ✓ 学生座位自动分配' as scenario;
SELECT '  ✓ 主监考和副监考' as scenario;
SELECT '  ✓ 时间冲突场景' as scenario;
SELECT '  ✓ 满容量和空考场边界情况' as scenario;
SELECT '  ✓ 最短(30分钟)和最长(180分钟)考试时长' as scenario;
SELECT '✓ 考试模块测试数据创建完成！' as status;

