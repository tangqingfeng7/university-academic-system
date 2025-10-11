-- ==================== 毕业审核模块测试数据 ====================
-- 创建日期: 2025-10-11
-- 描述: 毕业要求、学分汇总、毕业审核的测试数据

-- 1. 插入毕业要求数据
-- 计算机科学与技术专业 (major_id=1)
INSERT INTO graduation_requirement (created_at, updated_at, major_id, enrollment_year, total_credits_required, required_credits_required, elective_credits_required, practical_credits_required, additional_requirements)
VALUES 
(NOW(), NOW(), 1, 2020, 150.0, 110.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 1, 2021, 150.0, 110.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 1, 2022, 155.0, 115.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}'),
(NOW(), NOW(), 1, 2023, 155.0, 115.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}');

-- 软件工程专业 (major_id=2)
INSERT INTO graduation_requirement (created_at, updated_at, major_id, enrollment_year, total_credits_required, required_credits_required, elective_credits_required, practical_credits_required, additional_requirements)
VALUES 
(NOW(), NOW(), 2, 2020, 150.0, 108.0, 32.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 2, 2021, 150.0, 108.0, 32.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 2, 2022, 155.0, 112.0, 33.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}'),
(NOW(), NOW(), 2, 2023, 155.0, 112.0, 33.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}');

-- 信息安全专业 (major_id=3)
INSERT INTO graduation_requirement (created_at, updated_at, major_id, enrollment_year, total_credits_required, required_credits_required, elective_credits_required, practical_credits_required, additional_requirements)
VALUES 
(NOW(), NOW(), 3, 2020, 152.0, 112.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8,"securityCertification":"recommended"}'),
(NOW(), NOW(), 3, 2021, 152.0, 112.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8,"securityCertification":"recommended"}'),
(NOW(), NOW(), 3, 2022, 156.0, 116.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10,"securityCertification":"recommended"}'),
(NOW(), NOW(), 3, 2023, 156.0, 116.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10,"securityCertification":"recommended"}');

-- 数据科学与大数据技术专业 (major_id=4)
INSERT INTO graduation_requirement (created_at, updated_at, major_id, enrollment_year, total_credits_required, required_credits_required, elective_credits_required, practical_credits_required, additional_requirements)
VALUES 
(NOW(), NOW(), 4, 2020, 150.0, 110.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 4, 2021, 150.0, 110.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 4, 2022, 155.0, 113.0, 32.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}'),
(NOW(), NOW(), 4, 2023, 155.0, 113.0, 32.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}');

-- 人工智能专业 (major_id=5)
INSERT INTO graduation_requirement (created_at, updated_at, major_id, enrollment_year, total_credits_required, required_credits_required, elective_credits_required, practical_credits_required, additional_requirements)
VALUES 
(NOW(), NOW(), 5, 2021, 152.0, 112.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":8}'),
(NOW(), NOW(), 5, 2022, 156.0, 116.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}'),
(NOW(), NOW(), 5, 2023, 156.0, 116.0, 30.0, 10.0, '{"englishLevel":"CET-4","graduationThesis":true,"internshipWeeks":10}');

-- 2. 为部分学生初始化学分汇总数据
-- 注意: 实际环境中这些数据应该由系统自动计算生成
-- 这里只是为了测试展示添加少量示例数据

-- 2021级即将毕业的优秀学生 (学分已达标)
INSERT INTO student_credit_summary (created_at, updated_at, student_id, total_credits, required_credits, elective_credits, practical_credits, last_updated)
SELECT 
    NOW(),
    NOW(),
    s.id,
    CASE 
        WHEN s.id % 3 = 0 THEN 165.0  -- 超额完成
        WHEN s.id % 3 = 1 THEN 152.0  -- 刚好达标
        ELSE 145.0                     -- 接近达标
    END as total_credits,
    CASE 
        WHEN s.id % 3 = 0 THEN 118.0
        WHEN s.id % 3 = 1 THEN 112.0
        ELSE 106.0
    END as required_credits,
    CASE 
        WHEN s.id % 3 = 0 THEN 37.0
        WHEN s.id % 3 = 1 THEN 32.0
        ELSE 30.0
    END as elective_credits,
    CASE 
        WHEN s.id % 3 = 0 THEN 10.0
        WHEN s.id % 3 = 1 THEN 8.0
        ELSE 9.0
    END as practical_credits,
    NOW() as last_updated
FROM student s
WHERE s.enrollment_year = 2021 
  AND s.deleted = 0
LIMIT 30;

-- 2022级学生 (学分进度约75%)
INSERT INTO student_credit_summary (created_at, updated_at, student_id, total_credits, required_credits, elective_credits, practical_credits, last_updated)
SELECT 
    NOW(),
    NOW(),
    s.id,
    CASE 
        WHEN s.id % 4 = 0 THEN 120.0
        WHEN s.id % 4 = 1 THEN 110.0
        WHEN s.id % 4 = 2 THEN 100.0
        ELSE 95.0
    END as total_credits,
    CASE 
        WHEN s.id % 4 = 0 THEN 85.0
        WHEN s.id % 4 = 1 THEN 78.0
        WHEN s.id % 4 = 2 THEN 72.0
        ELSE 68.0
    END as required_credits,
    CASE 
        WHEN s.id % 4 = 0 THEN 28.0
        WHEN s.id % 4 = 1 THEN 25.0
        WHEN s.id % 4 = 2 THEN 22.0
        ELSE 20.0
    END as elective_credits,
    CASE 
        WHEN s.id % 4 = 0 THEN 7.0
        WHEN s.id % 4 = 1 THEN 7.0
        WHEN s.id % 4 = 2 THEN 6.0
        ELSE 7.0
    END as practical_credits,
    NOW() as last_updated
FROM student s
WHERE s.enrollment_year = 2022 
  AND s.deleted = 0
  AND s.id NOT IN (SELECT student_id FROM student_credit_summary)
LIMIT 20;

-- 3. 插入毕业审核记录
-- 为2021级部分学生添加毕业审核记录

-- 通过审核的学生 (学分达标)
INSERT INTO graduation_audit (created_at, updated_at, student_id, audit_year, total_credits, required_credits, elective_credits, practical_credits, status, audited_by, audited_at)
SELECT 
    NOW(),
    NOW(),
    scs.student_id,
    2025 as audit_year,
    scs.total_credits,
    scs.required_credits,
    scs.elective_credits,
    scs.practical_credits,
    'PASS' as status,
    1 as audited_by,  -- admin用户
    DATE_ADD(NOW(), INTERVAL -FLOOR(RAND() * 30) DAY) as audited_at
FROM student_credit_summary scs
JOIN student s ON s.id = scs.student_id
WHERE s.enrollment_year = 2021 
  AND scs.total_credits >= 150.0
  AND scs.required_credits >= 110.0
LIMIT 15;

-- 未通过审核的学生 (学分不足)
INSERT INTO graduation_audit (created_at, updated_at, student_id, audit_year, total_credits, required_credits, elective_credits, practical_credits, status, fail_reason, audited_by, audited_at)
SELECT 
    NOW(),
    NOW(),
    scs.student_id,
    2025 as audit_year,
    scs.total_credits,
    scs.required_credits,
    scs.elective_credits,
    scs.practical_credits,
    'FAIL' as status,
    CONCAT(
        'Graduation audit failed. ',
        CASE WHEN scs.total_credits < 150.0 THEN CONCAT('Total credits insufficient, need ', ROUND(150.0 - scs.total_credits, 1), ' more credits. ') ELSE '' END,
        CASE WHEN scs.required_credits < 110.0 THEN CONCAT('Required credits insufficient, need ', ROUND(110.0 - scs.required_credits, 1), ' more credits. ') ELSE '' END,
        CASE WHEN scs.elective_credits < 30.0 THEN CONCAT('Elective credits insufficient, need ', ROUND(30.0 - scs.elective_credits, 1), ' more credits. ') ELSE '' END,
        CASE WHEN scs.practical_credits < 10.0 THEN CONCAT('Practical credits insufficient, need ', ROUND(10.0 - scs.practical_credits, 1), ' more credits. ') ELSE '' END
    ) as fail_reason,
    1 as audited_by,
    DATE_ADD(NOW(), INTERVAL -FLOOR(RAND() * 30) DAY) as audited_at
FROM student_credit_summary scs
JOIN student s ON s.id = scs.student_id
WHERE s.enrollment_year = 2021 
  AND (scs.total_credits < 150.0 OR scs.required_credits < 110.0)
  AND scs.student_id NOT IN (SELECT student_id FROM graduation_audit)
LIMIT 10;

-- 待审核的学生
INSERT INTO graduation_audit (created_at, updated_at, student_id, audit_year, total_credits, required_credits, elective_credits, practical_credits, status, audited_at)
SELECT 
    NOW(),
    NOW(),
    scs.student_id,
    2025 as audit_year,
    scs.total_credits,
    scs.required_credits,
    scs.elective_credits,
    scs.practical_credits,
    'PENDING' as status,
    NOW() as audited_at
FROM student_credit_summary scs
JOIN student s ON s.id = scs.student_id
WHERE s.enrollment_year = 2021 
  AND scs.student_id NOT IN (SELECT student_id FROM graduation_audit)
LIMIT 5;

-- 显示插入结果统计
SELECT '毕业要求数据' as '数据类型', COUNT(*) as '记录数' FROM graduation_requirement
UNION ALL
SELECT '学分汇总数据', COUNT(*) FROM student_credit_summary
UNION ALL
SELECT '毕业审核记录', COUNT(*) FROM graduation_audit
UNION ALL
SELECT '- 通过审核', COUNT(*) FROM graduation_audit WHERE status = 'PASS'
UNION ALL
SELECT '- 未通过审核', COUNT(*) FROM graduation_audit WHERE status = 'FAIL'
UNION ALL
SELECT '- 待审核', COUNT(*) FROM graduation_audit WHERE status = 'PENDING';

