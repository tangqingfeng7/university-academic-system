-- 毕业要求配置数据
-- 为各专业配置毕业要求

-- 首先查看现有专业
-- SELECT id, name, code FROM major;

-- 为各专业添加毕业要求（2021-2024级）
INSERT INTO graduation_requirement (
    major_id,
    enrollment_year,
    total_credits_required,
    required_credits_required,
    elective_credits_required,
    practical_credits_required,
    additional_requirements,
    created_at
) VALUES
-- 1. 计算机科学与技术专业
(1, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 2. 软件工程专业
(2, 2024, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2023, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2022, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2021, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),

-- 3. 网络工程专业
(3, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 4. 人工智能专业
(4, 2024, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(4, 2023, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(4, 2022, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(4, 2021, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),

-- 5. 数据科学与大数据技术专业
(5, 2024, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2023, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2022, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2021, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),

-- 6. 数学与应用数学专业
(6, 2024, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(6, 2023, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(6, 2022, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(6, 2021, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 7. 信息与计算科学专业
(7, 2024, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(7, 2023, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(7, 2022, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(7, 2021, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 8. 统计学专业
(8, 2024, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(8, 2023, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(8, 2022, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(8, 2021, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 9. 物理学专业
(9, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(9, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(9, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(9, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 10. 应用物理学专业
(10, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(10, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(10, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(10, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 11. 化学专业
(11, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(11, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(11, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(11, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 12. 应用化学专业
(12, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(12, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(12, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(12, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 13. 生物科学专业
(13, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(13, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(13, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(13, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 14. 生物技术专业
(14, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(14, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(14, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(14, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 15. 电子工程专业
(15, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(15, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(15, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(15, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 16. 机械工程专业
(16, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(16, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(16, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(16, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 17. 土木工程专业
(17, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(17, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(17, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(17, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 18. 经济学专业 *** 这是刘芳霞同学的专业 ***
(18, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(18, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(18, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(18, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 19. 金融学专业
(19, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(19, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(19, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(19, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 20. 工商管理专业
(20, 2024, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(20, 2023, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(20, 2022, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(20, 2021, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 21. 会计学专业
(21, 2024, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(21, 2023, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(21, 2022, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(21, 2021, 145.0, 85.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW())
ON DUPLICATE KEY UPDATE major_id=major_id;

