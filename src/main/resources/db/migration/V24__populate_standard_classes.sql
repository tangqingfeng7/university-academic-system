-- ============================================================================
-- 标准班级数据填充
-- Version: V24
-- Description: 为所有专业和年级创建标准班级，解决字符编码问题
-- Author: Academic System Team
-- Date: 2025-01-13
-- ============================================================================

-- 设置字符集
SET NAMES utf8mb4;

-- 删除之前可能插入的损坏数据（保留id<=5的初始测试数据）
DELETE FROM class WHERE id > 5;

-- 计算机科学与技术 (major_id=1)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2022CS01', '计算机科学与技术2022级1班', 1, 2022, 40, NULL, 0, NOW(), NOW()),
('2021CS01', '计算机科学与技术2021级1班', 1, 2021, 40, NULL, 0, NOW(), NOW());

-- 软件工程 (major_id=2)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2022SE01', '软件工程2022级1班', 2, 2022, 40, NULL, 0, NOW(), NOW()),
('2021SE01', '软件工程2021级1班', 2, 2021, 40, NULL, 0, NOW(), NOW());

-- 网络工程 (major_id=3)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024NE01', '网络工程2024级1班', 3, 2024, 40, NULL, 0, NOW(), NOW()),
('2023NE01', '网络工程2023级1班', 3, 2023, 40, NULL, 0, NOW(), NOW()),
('2022NE01', '网络工程2022级1班', 3, 2022, 40, NULL, 0, NOW(), NOW()),
('2021NE01', '网络工程2021级1班', 3, 2021, 40, NULL, 0, NOW(), NOW());

-- 人工智能 (major_id=4)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024AI04', '人工智能2024级1班', 4, 2024, 40, NULL, 0, NOW(), NOW()),
('2023AI04', '人工智能2023级1班', 4, 2023, 40, NULL, 0, NOW(), NOW()),
('2022AI04', '人工智能2022级1班', 4, 2022, 40, NULL, 0, NOW(), NOW()),
('2021AI04', '人工智能2021级1班', 4, 2021, 40, NULL, 0, NOW(), NOW());

-- 数据科学与大数据技术 (major_id=5)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024DS01', '数据科学与大数据技术2024级1班', 5, 2024, 40, NULL, 0, NOW(), NOW()),
('2023DS01', '数据科学与大数据技术2023级1班', 5, 2023, 40, NULL, 0, NOW(), NOW()),
('2022DS01', '数据科学与大数据技术2022级1班', 5, 2022, 40, NULL, 0, NOW(), NOW()),
('2021DS01', '数据科学与大数据技术2021级1班', 5, 2021, 40, NULL, 0, NOW(), NOW());

-- 数学与应用数学 (major_id=6)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024MATH06', '数学与应用数学2024级1班', 6, 2024, 40, NULL, 0, NOW(), NOW()),
('2023MATH06', '数学与应用数学2023级1班', 6, 2023, 40, NULL, 0, NOW(), NOW()),
('2022MATH06', '数学与应用数学2022级1班', 6, 2022, 40, NULL, 0, NOW(), NOW()),
('2021MATH06', '数学与应用数学2021级1班', 6, 2021, 40, NULL, 0, NOW(), NOW());

-- 信息与计算科学 (major_id=7)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024IC01', '信息与计算科学2024级1班', 7, 2024, 40, NULL, 0, NOW(), NOW()),
('2023IC01', '信息与计算科学2023级1班', 7, 2023, 40, NULL, 0, NOW(), NOW()),
('2022IC01', '信息与计算科学2022级1班', 7, 2022, 40, NULL, 0, NOW(), NOW()),
('2021IC01', '信息与计算科学2021级1班', 7, 2021, 40, NULL, 0, NOW(), NOW());

-- 统计学 (major_id=8)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024STAT08', '统计学2024级1班', 8, 2024, 40, NULL, 0, NOW(), NOW()),
('2023STAT08', '统计学2023级1班', 8, 2023, 40, NULL, 0, NOW(), NOW()),
('2022STAT08', '统计学2022级1班', 8, 2022, 40, NULL, 0, NOW(), NOW()),
('2021STAT08', '统计学2021级1班', 8, 2021, 40, NULL, 0, NOW(), NOW());

-- 物理学 (major_id=9)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024PHY09', '物理学2024级1班', 9, 2024, 40, NULL, 0, NOW(), NOW()),
('2023PHY09', '物理学2023级1班', 9, 2023, 40, NULL, 0, NOW(), NOW()),
('2022PHY09', '物理学2022级1班', 9, 2022, 40, NULL, 0, NOW(), NOW()),
('2021PHY09', '物理学2021级1班', 9, 2021, 40, NULL, 0, NOW(), NOW());

-- 计算机科学与技术 (major_id=10)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024CS10', '计算机科学与技术2024级2班', 10, 2024, 40, NULL, 0, NOW(), NOW()),
('2023CS10', '计算机科学与技术2023级2班', 10, 2023, 40, NULL, 0, NOW(), NOW()),
('2022CS10', '计算机科学与技术2022级2班', 10, 2022, 40, NULL, 0, NOW(), NOW()),
('2021CS10', '计算机科学与技术2021级2班', 10, 2021, 40, NULL, 0, NOW(), NOW());

-- 软件工程 (major_id=11)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024SE11', '软件工程2024级2班', 11, 2024, 40, NULL, 0, NOW(), NOW()),
('2023SE11', '软件工程2023级2班', 11, 2023, 40, NULL, 0, NOW(), NOW()),
('2022SE11', '软件工程2022级2班', 11, 2022, 40, NULL, 0, NOW(), NOW()),
('2021SE11', '软件工程2021级2班', 11, 2021, 40, NULL, 0, NOW(), NOW());

-- 人工智能 (major_id=12)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024AI12', '人工智能2024级2班', 12, 2024, 40, NULL, 0, NOW(), NOW()),
('2023AI12', '人工智能2023级2班', 12, 2023, 40, NULL, 0, NOW(), NOW()),
('2022AI12', '人工智能2022级2班', 12, 2022, 40, NULL, 0, NOW(), NOW()),
('2021AI12', '人工智能2021级2班', 12, 2021, 40, NULL, 0, NOW(), NOW());

-- 数学与应用数学 (major_id=13)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024MATH13', '数学与应用数学2024级2班', 13, 2024, 40, NULL, 0, NOW(), NOW()),
('2023MATH13', '数学与应用数学2023级2班', 13, 2023, 40, NULL, 0, NOW(), NOW()),
('2022MATH13', '数学与应用数学2022级2班', 13, 2022, 40, NULL, 0, NOW(), NOW()),
('2021MATH13', '数学与应用数学2021级2班', 13, 2021, 40, NULL, 0, NOW(), NOW());

-- 统计学 (major_id=14)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024STAT14', '统计学2024级2班', 14, 2024, 40, NULL, 0, NOW(), NOW()),
('2023STAT14', '统计学2023级2班', 14, 2023, 40, NULL, 0, NOW(), NOW()),
('2022STAT14', '统计学2022级2班', 14, 2022, 40, NULL, 0, NOW(), NOW()),
('2021STAT14', '统计学2021级2班', 14, 2021, 40, NULL, 0, NOW(), NOW());

-- 物理学 (major_id=15)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024PHY15', '物理学2024级2班', 15, 2024, 40, NULL, 0, NOW(), NOW()),
('2023PHY15', '物理学2023级2班', 15, 2023, 40, NULL, 0, NOW(), NOW()),
('2022PHY15', '物理学2022级2班', 15, 2022, 40, NULL, 0, NOW(), NOW()),
('2021PHY15', '物理学2021级2班', 15, 2021, 40, NULL, 0, NOW(), NOW());

-- 电子信息工程 (major_id=16)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024EE01', '电子信息工程2024级1班', 16, 2024, 40, NULL, 0, NOW(), NOW()),
('2023EE01', '电子信息工程2023级1班', 16, 2023, 40, NULL, 0, NOW(), NOW()),
('2022EE01', '电子信息工程2022级1班', 16, 2022, 40, NULL, 0, NOW(), NOW()),
('2021EE01', '电子信息工程2021级1班', 16, 2021, 40, NULL, 0, NOW(), NOW());

-- 化学 (major_id=17)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024CHEM01', '化学2024级1班', 17, 2024, 40, NULL, 0, NOW(), NOW()),
('2023CHEM01', '化学2023级1班', 17, 2023, 40, NULL, 0, NOW(), NOW()),
('2022CHEM01', '化学2022级1班', 17, 2022, 40, NULL, 0, NOW(), NOW()),
('2021CHEM01', '化学2021级1班', 17, 2021, 40, NULL, 0, NOW(), NOW());

-- 化学工程与工艺 (major_id=18)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024CHE01', '化学工程与工艺2024级1班', 18, 2024, 40, NULL, 0, NOW(), NOW()),
('2023CHE01', '化学工程与工艺2023级1班', 18, 2023, 40, NULL, 0, NOW(), NOW()),
('2022CHE01', '化学工程与工艺2022级1班', 18, 2022, 40, NULL, 0, NOW(), NOW()),
('2021CHE01', '化学工程与工艺2021级1班', 18, 2021, 40, NULL, 0, NOW(), NOW());

-- 生物科学 (major_id=19)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024BIO01', '生物科学2024级1班', 19, 2024, 40, NULL, 0, NOW(), NOW()),
('2023BIO01', '生物科学2023级1班', 19, 2023, 40, NULL, 0, NOW(), NOW()),
('2022BIO01', '生物科学2022级1班', 19, 2022, 40, NULL, 0, NOW(), NOW()),
('2021BIO01', '生物科学2021级1班', 19, 2021, 40, NULL, 0, NOW(), NOW());

-- 生物技术 (major_id=20)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024BT01', '生物技术2024级1班', 20, 2024, 40, NULL, 0, NOW(), NOW()),
('2023BT01', '生物技术2023级1班', 20, 2023, 40, NULL, 0, NOW(), NOW()),
('2022BT01', '生物技术2022级1班', 20, 2022, 40, NULL, 0, NOW(), NOW()),
('2021BT01', '生物技术2021级1班', 20, 2021, 40, NULL, 0, NOW(), NOW());

-- 经济学 (major_id=21)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024ECON01', '经济学2024级1班', 21, 2024, 40, NULL, 0, NOW(), NOW()),
('2023ECON01', '经济学2023级1班', 21, 2023, 40, NULL, 0, NOW(), NOW()),
('2022ECON01', '经济学2022级1班', 21, 2022, 40, NULL, 0, NOW(), NOW()),
('2021ECON01', '经济学2021级1班', 21, 2021, 40, NULL, 0, NOW(), NOW());

-- 金融学 (major_id=22)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024FIN01', '金融学2024级1班', 22, 2024, 40, NULL, 0, NOW(), NOW()),
('2023FIN01', '金融学2023级1班', 22, 2023, 40, NULL, 0, NOW(), NOW()),
('2022FIN01', '金融学2022级1班', 22, 2022, 40, NULL, 0, NOW(), NOW()),
('2021FIN01', '金融学2021级1班', 22, 2021, 40, NULL, 0, NOW(), NOW());

-- 法学 (major_id=23)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024LAW01', '法学2024级1班', 23, 2024, 40, NULL, 0, NOW(), NOW()),
('2023LAW01', '法学2023级1班', 23, 2023, 40, NULL, 0, NOW(), NOW()),
('2022LAW01', '法学2022级1班', 23, 2022, 40, NULL, 0, NOW(), NOW()),
('2021LAW01', '法学2021级1班', 23, 2021, 40, NULL, 0, NOW(), NOW());

-- 汉语言文学 (major_id=24)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024LIT01', '汉语言文学2024级1班', 24, 2024, 40, NULL, 0, NOW(), NOW()),
('2023LIT01', '汉语言文学2023级1班', 24, 2023, 40, NULL, 0, NOW(), NOW()),
('2022LIT01', '汉语言文学2022级1班', 24, 2022, 40, NULL, 0, NOW(), NOW()),
('2021LIT01', '汉语言文学2021级1班', 24, 2021, 40, NULL, 0, NOW(), NOW());

-- 英语 (major_id=25)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024ENG01', '英语2024级1班', 25, 2024, 40, NULL, 0, NOW(), NOW()),
('2023ENG01', '英语2023级1班', 25, 2023, 40, NULL, 0, NOW(), NOW()),
('2022ENG01', '英语2022级1班', 25, 2022, 40, NULL, 0, NOW(), NOW()),
('2021ENG01', '英语2021级1班', 25, 2021, 40, NULL, 0, NOW(), NOW());

-- 美术学 (major_id=26)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024ART01', '美术学2024级1班', 26, 2024, 40, NULL, 0, NOW(), NOW()),
('2023ART01', '美术学2023级1班', 26, 2023, 40, NULL, 0, NOW(), NOW()),
('2022ART01', '美术学2022级1班', 26, 2022, 40, NULL, 0, NOW(), NOW()),
('2021ART01', '美术学2021级1班', 26, 2021, 40, NULL, 0, NOW(), NOW());

-- 音乐学 (major_id=27)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024MUS01', '音乐学2024级1班', 27, 2024, 40, NULL, 0, NOW(), NOW()),
('2023MUS01', '音乐学2023级1班', 27, 2023, 40, NULL, 0, NOW(), NOW()),
('2022MUS01', '音乐学2022级1班', 27, 2022, 40, NULL, 0, NOW(), NOW()),
('2021MUS01', '音乐学2021级1班', 27, 2021, 40, NULL, 0, NOW(), NOW());

-- 临床医学 (major_id=28)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024MED01', '临床医学2024级1班', 28, 2024, 40, NULL, 0, NOW(), NOW()),
('2023MED01', '临床医学2023级1班', 28, 2023, 40, NULL, 0, NOW(), NOW()),
('2022MED01', '临床医学2022级1班', 28, 2022, 40, NULL, 0, NOW(), NOW()),
('2021MED01', '临床医学2021级1班', 28, 2021, 40, NULL, 0, NOW(), NOW());

-- 护理学 (major_id=29)
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, deleted, created_at, updated_at) VALUES
('2024NUR01', '护理学2024级1班', 29, 2024, 40, NULL, 0, NOW(), NOW()),
('2023NUR01', '护理学2023级1班', 29, 2023, 40, NULL, 0, NOW(), NOW()),
('2022NUR01', '护理学2022级1班', 29, 2022, 40, NULL, 0, NOW(), NOW()),
('2021NUR01', '护理学2021级1班', 29, 2021, 40, NULL, 0, NOW(), NOW());

-- 更新所有学生的班级名称为规范格式
UPDATE student s
INNER JOIN class c ON c.major_id = s.major_id AND c.enrollment_year = s.enrollment_year
SET s.class_name = c.class_name
WHERE s.deleted = 0;

-- 显示统计信息
SELECT 
    '班级创建完成' as status,
    COUNT(*) as total_classes
FROM class 
WHERE deleted = 0;

SELECT 
    '学生更新完成' as status,
    COUNT(*) as total_students,
    COUNT(DISTINCT class_name) as unique_classes
FROM student 
WHERE deleted = 0;

