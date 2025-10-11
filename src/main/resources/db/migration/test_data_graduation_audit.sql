-- 毕业审核测试数据
-- 为部分学生创建毕业审核记录

-- 插入毕业审核记录（2025届学生）
INSERT INTO graduation_audit (
    student_id,
    audit_year,
    total_credits,
    required_credits,
    elective_credits,
    practical_credits,
    status,
    fail_reason,
    audited_by,
    audited_at,
    created_at
) VALUES
-- 学生ID 3-10 的审核记录（假设这些是2021级学生，现在大四）
(3, 2025, 165.0, 110.0, 42.0, 13.0, 'PASS', NULL, 1, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(4, 2025, 158.0, 108.0, 38.0, 12.0, 'PASS', NULL, 1, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(5, 2025, 145.0, 95.0, 38.0, 12.0, 'FAIL', '总学分不足，还差10学分。必修课《数据结构》未通过。', 1, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(6, 2025, 162.0, 105.0, 40.0, 17.0, 'PASS', NULL, 1, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(7, 2025, 155.0, 100.0, 40.0, 15.0, 'PASS', NULL, 1, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(8, 2025, 138.0, 90.0, 35.0, 13.0, 'FAIL', '选修学分不足，还差5学分。', 1, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(9, 2025, 168.0, 112.0, 42.0, 14.0, 'PASS', NULL, 1, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(10, 2025, 152.0, 98.0, 40.0, 14.0, 'PENDING', NULL, NULL, NULL, NOW() - INTERVAL 8 DAY),

-- 2024届毕业生的历史审核记录（已毕业）
(11, 2024, 162.0, 110.0, 40.0, 12.0, 'PASS', NULL, 1, NOW() - INTERVAL 180 DAY, NOW() - INTERVAL 180 DAY),
(12, 2024, 158.0, 105.0, 41.0, 12.0, 'PASS', NULL, 1, NOW() - INTERVAL 180 DAY, NOW() - INTERVAL 180 DAY),
(13, 2024, 155.0, 102.0, 40.0, 13.0, 'PASS', NULL, 1, NOW() - INTERVAL 180 DAY, NOW() - INTERVAL 180 DAY),

-- 延期毕业的学生
(14, 2024, 142.0, 88.0, 40.0, 14.0, 'DEFERRED', '必修学分不足，延期至2025年毕业。需完成《软件工程》和《算法设计》两门必修课。', 1, NOW() - INTERVAL 180 DAY, NOW() - INTERVAL 180 DAY),
(14, 2025, 160.0, 105.0, 40.0, 15.0, 'PASS', NULL, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),

-- 在读学生的毕业预审核（2024级学生，现在大一）
(33, 2028, 30.0, 28.0, 2.0, 0.0, 'PENDING', NULL, NULL, NULL, NOW() - INTERVAL 1 DAY),

-- 更多2025届学生的审核记录
(15, 2025, 170.0, 115.0, 42.0, 13.0, 'PASS', NULL, 1, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(16, 2025, 165.0, 110.0, 42.0, 13.0, 'PASS', NULL, 1, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(17, 2025, 148.0, 92.0, 42.0, 14.0, 'FAIL', '必修课《操作系统》、《计算机网络》未通过，需要重修。', 1, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(18, 2025, 163.0, 108.0, 42.0, 13.0, 'PASS', NULL, 1, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY)
ON DUPLICATE KEY UPDATE student_id=student_id;

-- 更新学生学分汇总表（如果存在）
INSERT INTO student_credit_summary (
    student_id,
    total_credits,
    required_credits,
    elective_credits,
    practical_credits,
    last_updated,
    created_at
) VALUES
(3, 165.0, 110.0, 42.0, 13.0, NOW(), NOW()),
(4, 158.0, 108.0, 38.0, 12.0, NOW(), NOW()),
(5, 145.0, 95.0, 38.0, 12.0, NOW(), NOW()),
(6, 162.0, 105.0, 40.0, 17.0, NOW(), NOW()),
(7, 155.0, 100.0, 40.0, 15.0, NOW(), NOW()),
(8, 138.0, 90.0, 35.0, 13.0, NOW(), NOW()),
(9, 168.0, 112.0, 42.0, 14.0, NOW(), NOW()),
(10, 152.0, 98.0, 40.0, 14.0, NOW(), NOW()),
(33, 30.0, 28.0, 2.0, 0.0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    total_credits=VALUES(total_credits),
    required_credits=VALUES(required_credits),
    elective_credits=VALUES(elective_credits),
    practical_credits=VALUES(practical_credits),
    last_updated=NOW();

