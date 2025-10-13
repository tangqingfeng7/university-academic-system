-- 补全缺失的班级数据
USE academic_system;

SET NAMES utf8mb4;

-- 为所有缺失的专业+年级组合创建班级
-- 物理学 (major_id=9) - 和major_id=15重复了，合并处理
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024PHY02', '物理学2024级2班', 9, 2024, 40, 0, NOW(), NOW()),
('2023PHY02', '物理学2023级2班', 9, 2023, 40, 0, NOW(), NOW()),
('2022PHY02', '物理学2022级2班', 9, 2022, 40, 0, NOW(), NOW()),
('2021PHY02', '物理学2021级2班', 9, 2021, 40, 0, NOW(), NOW());

-- 数学与应用数学 (major_id=6) - 和major_id=13重复
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024MATH02', '数学与应用数学2024级2班', 6, 2024, 40, 0, NOW(), NOW()),
('2023MATH02', '数学与应用数学2023级2班', 6, 2023, 40, 0, NOW(), NOW()),
('2022MATH02', '数学与应用数学2022级2班', 6, 2022, 40, 0, NOW(), NOW()),
('2021MATH02', '数学与应用数学2021级2班', 6, 2021, 40, 0, NOW(), NOW());

-- 统计学 (major_id=8) - 和major_id=14重复
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024STAT02', '统计学2024级2班', 8, 2024, 40, 0, NOW(), NOW()),
('2023STAT02', '统计学2023级2班', 8, 2023, 40, 0, NOW(), NOW()),
('2022STAT02', '统计学2022级2班', 8, 2022, 40, 0, NOW(), NOW()),
('2021STAT02', '统计学2021级2班', 8, 2021, 40, 0, NOW(), NOW());

-- 计算机科学与技术 (major_id=10) - 需要补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024CS10', '计算机科学与技术2024级3班', 10, 2024, 40, 0, NOW(), NOW()),
('2023CS10', '计算机科学与技术2023级2班', 10, 2023, 40, 0, NOW(), NOW()),
('2022CS10', '计算机科学与技术2022级2班', 10, 2022, 40, 0, NOW(), NOW()),
('2021CS10', '计算机科学与技术2021级2班', 10, 2021, 40, 0, NOW(), NOW());

-- 软件工程 (major_id=11) - 需要补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024SE11', '软件工程2024级2班', 11, 2024, 40, 0, NOW(), NOW()),
('2023SE11', '软件工程2023级2班', 11, 2023, 40, 0, NOW(), NOW()),
('2022SE11', '软件工程2022级2班', 11, 2022, 40, 0, NOW(), NOW()),
('2021SE11', '软件工程2021级2班', 11, 2021, 40, 0, NOW(), NOW());

-- 人工智能 (major_id=4) - 需要补充
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024AI04', '人工智能2024级2班', 4, 2024, 40, 0, NOW(), NOW()),
('2023AI04', '人工智能2023级2班', 4, 2023, 40, 0, NOW(), NOW()),
('2022AI04', '人工智能2022级2班', 4, 2022, 40, 0, NOW(), NOW()),
('2021AI04', '人工智能2021级2班', 4, 2021, 40, 0, NOW(), NOW());

-- 网络工程 (major_id=3)
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024NE01', '网络工程2024级1班', 3, 2024, 40, 0, NOW(), NOW()),
('2023NE01', '网络工程2023级1班', 3, 2023, 40, 0, NOW(), NOW()),
('2022NE01', '网络工程2022级1班', 3, 2022, 40, 0, NOW(), NOW()),
('2021NE01', '网络工程2021级1班', 3, 2021, 40, 0, NOW(), NOW());

-- 信息与计算科学 (major_id=7)
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024IC01', '信息与计算科学2024级1班', 7, 2024, 40, 0, NOW(), NOW()),
('2023IC01', '信息与计算科学2023级1班', 7, 2023, 40, 0, NOW(), NOW()),
('2022IC01', '信息与计算科学2022级1班', 7, 2022, 40, 0, NOW(), NOW()),
('2021IC01', '信息与计算科学2021级1班', 7, 2021, 40, 0, NOW(), NOW());

-- 数据科学与大数据技术 (major_id=5)
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2024DS01', '数据科学与大数据技术2024级1班', 5, 2024, 40, 0, NOW(), NOW()),
('2023DS01', '数据科学与大数据技术2023级1班', 5, 2023, 40, 0, NOW(), NOW()),
('2022DS01', '数据科学与大数据技术2022级1班', 5, 2022, 40, 0, NOW(), NOW()),
('2021DS01', '数据科学与大数据技术2021级1班', 5, 2021, 40, 0, NOW(), NOW());

-- 金融学 (major_id=22) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023FIN01', '金融学2023级1班', 22, 2023, 40, 0, NOW(), NOW()),
('2022FIN01', '金融学2022级1班', 22, 2022, 40, 0, NOW(), NOW()),
('2021FIN01', '金融学2021级1班', 22, 2021, 40, 0, NOW(), NOW());

-- 法学 (major_id=23) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023LAW01', '法学2023级1班', 23, 2023, 40, 0, NOW(), NOW()),
('2022LAW01', '法学2022级1班', 23, 2022, 40, 0, NOW(), NOW()),
('2021LAW01', '法学2021级1班', 23, 2021, 40, 0, NOW(), NOW());

-- 汉语言文学 (major_id=24) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023LIT01', '汉语言文学2023级1班', 24, 2023, 40, 0, NOW(), NOW()),
('2022LIT01', '汉语言文学2022级1班', 24, 2022, 40, 0, NOW(), NOW()),
('2021LIT01', '汉语言文学2021级1班', 24, 2021, 40, 0, NOW(), NOW());

-- 英语 (major_id=25) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023ENG01', '英语2023级1班', 25, 2023, 40, 0, NOW(), NOW()),
('2022ENG01', '英语2022级1班', 25, 2022, 40, 0, NOW(), NOW()),
('2021ENG01', '英语2021级1班', 25, 2021, 40, 0, NOW(), NOW());

-- 美术学 (major_id=26) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023ART01', '美术学2023级1班', 26, 2023, 40, 0, NOW(), NOW()),
('2022ART01', '美术学2022级1班', 26, 2022, 40, 0, NOW(), NOW()),
('2021ART01', '美术学2021级1班', 26, 2021, 40, 0, NOW(), NOW());

-- 音乐学 (major_id=27) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023MUS01', '音乐学2023级1班', 27, 2023, 40, 0, NOW(), NOW()),
('2022MUS01', '音乐学2022级1班', 27, 2022, 40, 0, NOW(), NOW()),
('2021MUS01', '音乐学2021级1班', 27, 2021, 40, 0, NOW(), NOW());

-- 临床医学 (major_id=28) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023MED01', '临床医学2023级1班', 28, 2023, 40, 0, NOW(), NOW()),
('2022MED01', '临床医学2022级1班', 28, 2022, 40, 0, NOW(), NOW()),
('2021MED01', '临床医学2021级1班', 28, 2021, 40, 0, NOW(), NOW());

-- 护理学 (major_id=29) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2023NUR01', '护理学2023级1班', 29, 2023, 40, 0, NOW(), NOW()),
('2022NUR01', '护理学2022级1班', 29, 2022, 40, 0, NOW(), NOW()),
('2021NUR01', '护理学2021级1班', 29, 2021, 40, 0, NOW(), NOW());

-- 生物科学 (major_id=19) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2021BIO01', '生物科学2021级1班', 19, 2021, 40, 0, NOW(), NOW());

-- 生物技术 (major_id=20) 补充年份
INSERT IGNORE INTO class (class_code, class_name, major_id, enrollment_year, capacity, deleted, created_at, updated_at) VALUES
('2022BT01', '生物技术2022级1班', 20, 2022, 40, 0, NOW(), NOW()),
('2021BT01', '生物技术2021级1班', 20, 2021, 40, 0, NOW(), NOW());

-- 统一更新所有学生的班级名称
UPDATE student s
JOIN major m ON s.major_id = m.id
LEFT JOIN class c ON c.major_id = s.major_id AND c.enrollment_year = s.enrollment_year
SET s.class_name = c.class_name
WHERE s.deleted = 0 AND c.id IS NOT NULL;

-- 显示更新统计
SELECT 
    '更新完成' as status,
    COUNT(*) as total_students,
    SUM(CASE WHEN class_name LIKE '%级%班' THEN 1 ELSE 0 END) as normalized_count,
    SUM(CASE WHEN class_name NOT LIKE '%级%班' THEN 1 ELSE 0 END) as unnormalized_count
FROM student 
WHERE deleted = 0;

