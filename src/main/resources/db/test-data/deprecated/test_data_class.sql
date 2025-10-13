-- ============================================================================
-- 班级管理模块测试数据
-- Author: Academic System Team
-- Date: 2025-01-13
-- ============================================================================

USE academic_system;

-- 清空现有数据（如果存在）
DELETE FROM class WHERE id IN (1, 2, 3, 4, 5);

-- 插入测试数据
-- 注意：需要确保major表中存在对应的major_id
INSERT INTO class (id, class_code, class_name, major_id, enrollment_year, capacity, counselor_id, remarks, deleted, created_at, updated_at) VALUES
(1, '2024CS01', '计算机科学与技术2024级1班', 1, 2024, 40, NULL, '计算机科学与技术专业第1班', 0, NOW(), NOW()),
(2, '2024CS02', '计算机科学与技术2024级2班', 1, 2024, 40, NULL, '计算机科学与技术专业第2班', 0, NOW(), NOW()),
(3, '2024SE01', '软件工程2024级1班', 2, 2024, 35, NULL, '软件工程专业第1班', 0, NOW(), NOW()),
(4, '2023CS01', '计算机科学与技术2023级1班', 1, 2023, 40, NULL, '计算机科学与技术专业第1班', 0, NOW(), NOW()),
(5, '2023SE01', '软件工程2023级1班', 2, 2023, 35, NULL, '软件工程专业第1班', 0, NOW(), NOW());

-- 验证数据插入
SELECT COUNT(*) as total_classes FROM class;
SELECT * FROM class ORDER BY enrollment_year DESC, class_code ASC;

