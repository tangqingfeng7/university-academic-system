-- 毕业要求配置数据
-- 为各专业配置毕业要求

-- 首先查看现有专业
-- SELECT id, name, code FROM major;

-- 为各专业添加毕业要求（2024级）
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
-- 计算机科学与技术专业 (假设ID=1)
(1, 2024, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2023, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2022, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(1, 2021, 160.0, 100.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 软件工程专业 (假设ID=2)
(2, 2024, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2023, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2022, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),
(2, 2021, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "internship": true}', NOW()),

-- 信息管理与信息系统专业 (假设ID=3)
(3, 2024, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2023, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2022, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(3, 2021, 155.0, 95.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 电子商务专业 (假设ID=4)
(4, 2024, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(4, 2023, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(4, 2022, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),
(4, 2021, 150.0, 90.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4"}', NOW()),

-- 数据科学与大数据技术专业 (假设ID=5)
(5, 2024, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2023, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2022, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW()),
(5, 2021, 165.0, 105.0, 40.0, 20.0, '{"thesis": true, "english_level": "CET-4", "project": true}', NOW())
ON DUPLICATE KEY UPDATE major_id=major_id;

