-- =============================================
-- 插入奖学金初始数据
-- =============================================

-- 清空现有数据（可选，谨慎使用）
-- TRUNCATE TABLE scholarship_award;
-- TRUNCATE TABLE scholarship_approval;
-- TRUNCATE TABLE scholarship_application;
-- TRUNCATE TABLE scholarship;

-- 插入默认奖学金类型
INSERT INTO scholarship (name, level, amount, quota, min_gpa, min_credits, description, active, created_at) VALUES
('国家奖学金', 'NATIONAL', 8000.00, 10, 3.8, 120, '面向品学兼优的学生，奖励金额8000元', TRUE, NOW()),
('国家励志奖学金', 'NATIONAL', 5000.00, 50, 3.5, 100, '面向家庭困难且品学兼优的学生，奖励金额5000元', TRUE, NOW()),
('省政府奖学金', 'PROVINCIAL', 6000.00, 30, 3.6, 110, '由省政府出资设立，奖励金额6000元', TRUE, NOW()),
('校级一等奖学金', 'UNIVERSITY', 3000.00, 100, 3.5, 90, '学校设立的一等奖学金，奖励金额3000元', TRUE, NOW()),
('校级二等奖学金', 'UNIVERSITY', 2000.00, 200, 3.2, 80, '学校设立的二等奖学金，奖励金额2000元', TRUE, NOW()),
('校级三等奖学金', 'UNIVERSITY', 1000.00, 300, 3.0, 70, '学校设立的三等奖学金，奖励金额1000元', TRUE, NOW()),
('院系优秀奖学金', 'DEPARTMENT', 1500.00, 50, 3.0, 60, '院系设立的优秀奖学金，奖励金额1500元', TRUE, NOW()),
('创新创业奖学金', 'UNIVERSITY', 2500.00, 80, 3.0, 70, '奖励在创新创业方面有突出表现的学生', TRUE, NOW())
ON DUPLICATE KEY UPDATE name=name;

SELECT '奖学金初始数据插入完成' AS message;
SELECT id, name, level, amount, quota FROM scholarship;

