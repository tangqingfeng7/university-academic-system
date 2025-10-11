-- =============================================
-- 奖学金评定模块测试数据
-- =============================================

-- 注意：此脚本假设已经存在学生数据（student表）

-- 1. 插入奖学金申请测试数据
-- 假设学生ID 1-10 为测试学生
INSERT INTO scholarship_application (
    scholarship_id, student_id, academic_year, gpa, total_credits,
    comprehensive_score, personal_statement, status, submitted_at
) VALUES
-- 国家奖学金申请（scholarship_id=1）
(1, 1, '2024-2025', 3.95, 145.0, 96.5, '我在学习和实践中不断成长，积极参加学术竞赛，获得多项荣誉。希望能获得国家奖学金的支持，继续努力学习。', 'DEPT_APPROVED', '2024-10-01 10:00:00'),
(1, 2, '2024-2025', 3.88, 142.0, 94.2, '通过不懈努力，我在学业上取得了优异成绩，同时积极参与社会实践活动。', 'COUNSELOR_APPROVED', '2024-10-02 11:30:00'),
(1, 3, '2024-2025', 3.82, 140.0, 92.8, '我热爱学习，追求卓越，在各方面都严格要求自己。', 'PENDING', '2024-10-03 09:15:00'),

-- 国家励志奖学金申请（scholarship_id=2）
(2, 4, '2024-2025', 3.65, 135.0, 88.5, '虽然家庭条件困难，但我从未放弃学习。通过勤工俭学，我既减轻了家庭负担，又取得了优异成绩。', 'DEPT_APPROVED', '2024-10-01 14:20:00'),
(2, 5, '2024-2025', 3.58, 132.0, 86.7, '我来自贫困家庭，深知学习机会的珍贵。我会继续努力，不辜负国家和学校的培养。', 'COUNSELOR_APPROVED', '2024-10-02 15:45:00'),
(2, 6, '2024-2025', 3.52, 128.0, 84.9, '面对困难，我选择坚强面对。学习是改变命运的最好途径。', 'PENDING', '2024-10-03 16:30:00'),

-- 省政府奖学金申请（scholarship_id=3）
(3, 7, '2024-2025', 3.72, 138.0, 90.3, '我积极参加各类学术活动，在专业领域有深入研究。', 'DEPT_APPROVED', '2024-10-01 13:00:00'),
(3, 8, '2024-2025', 3.68, 136.0, 89.1, '通过系统的学习和实践，我的专业能力得到了全面提升。', 'COUNSELOR_APPROVED', '2024-10-02 14:15:00'),

-- 校级一等奖学金申请（scholarship_id=4）
(4, 9, '2024-2025', 3.55, 130.0, 87.0, '我在学业上刻苦努力，各科成绩均衡发展。', 'DEPT_APPROVED', '2024-10-01 10:30:00'),
(4, 10, '2024-2025', 3.52, 128.0, 85.8, '通过一年的努力，我的学习成绩有了显著提高。', 'COUNSELOR_APPROVED', '2024-10-02 11:45:00'),
(4, 1, '2024-2025', 3.58, 132.0, 87.5, '我热爱学习，追求进步，希望能获得奖学金的认可。', 'PENDING', '2024-10-03 10:00:00'),

-- 校级二等奖学金申请（scholarship_id=5）
(5, 2, '2024-2025', 3.35, 125.0, 82.0, '我在学习上认真努力，积极参加课外活动。', 'DEPT_APPROVED', '2024-10-01 15:00:00'),
(5, 3, '2024-2025', 3.28, 122.0, 80.5, '通过持续努力，我的综合素质得到了提升。', 'COUNSELOR_APPROVED', '2024-10-02 16:20:00'),
(5, 4, '2024-2025', 3.25, 120.0, 79.8, '我会继续努力学习，争取更大的进步。', 'PENDING', '2024-10-03 11:30:00'),

-- 校级三等奖学金申请（scholarship_id=6）
(6, 5, '2024-2025', 3.15, 115.0, 77.5, '我在学习上保持稳定，积极参与班级活动。', 'DEPT_APPROVED', '2024-10-01 16:00:00'),
(6, 6, '2024-2025', 3.08, 112.0, 75.9, '我会继续努力，不断提高自己的学习成绩。', 'COUNSELOR_APPROVED', '2024-10-02 17:00:00'),

-- 院系优秀奖学金申请（scholarship_id=7）
(7, 7, '2024-2025', 3.05, 110.0, 75.0, '我积极参加院系组织的各项活动，表现突出。', 'DEPT_APPROVED', '2024-10-01 11:00:00'),
(7, 8, '2024-2025', 3.02, 108.0, 74.3, '在院系中，我努力发挥自己的特长，服务同学。', 'COUNSELOR_APPROVED', '2024-10-02 12:30:00'),

-- 创新创业奖学金申请（scholarship_id=8）
(8, 9, '2024-2025', 3.18, 118.0, 78.2, '我参与了多个创新创业项目，积累了丰富的实践经验。', 'DEPT_APPROVED', '2024-10-01 14:00:00'),
(8, 10, '2024-2025', 3.12, 115.0, 76.8, '我热爱创新，积极探索新技术、新模式。', 'COUNSELOR_APPROVED', '2024-10-02 15:00:00');

-- 2. 插入审批记录测试数据
-- 为已审批的申请添加审批记录

-- 国家奖学金（application_id=1）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(1, 1, 2, 'APPROVE', '该生学习成绩优异，综合素质突出，同意推荐。', '2024-10-01 15:00:00'),
(1, 2, 1, 'APPROVE', '经院系评审，该生符合国家奖学金条件，同意上报。', '2024-10-02 10:00:00');

-- 国家奖学金（application_id=2）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(2, 1, 2, 'APPROVE', '该生成绩优秀，表现良好，推荐参评。', '2024-10-02 16:00:00');

-- 国家励志奖学金（application_id=4）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(4, 1, 2, 'APPROVE', '该生家庭困难，学习刻苦，成绩优良，同意推荐。', '2024-10-01 17:00:00'),
(4, 2, 1, 'APPROVE', '符合国家励志奖学金评定条件，同意。', '2024-10-02 11:00:00');

-- 国家励志奖学金（application_id=5）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(5, 1, 2, 'APPROVE', '该生品学兼优，家庭困难，推荐评选。', '2024-10-02 17:30:00');

-- 省政府奖学金（application_id=7）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(7, 1, 2, 'APPROVE', '该生学习认真，成绩优秀，推荐申请。', '2024-10-01 16:30:00'),
(7, 2, 1, 'APPROVE', '符合省政府奖学金标准，同意。', '2024-10-02 12:00:00');

-- 省政府奖学金（application_id=8）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(8, 1, 2, 'APPROVE', '该生综合表现优秀，推荐申请省政府奖学金。', '2024-10-02 18:00:00');

-- 校级一等奖学金（application_id=9）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(9, 1, 2, 'APPROVE', '该生成绩优良，表现突出，推荐。', '2024-10-01 17:00:00'),
(9, 2, 1, 'APPROVE', '同意授予校级一等奖学金。', '2024-10-02 13:00:00');

-- 校级一等奖学金（application_id=10）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(10, 1, 2, 'APPROVE', '该生学习进步明显，推荐申请。', '2024-10-02 18:30:00');

-- 校级二等奖学金（application_id=12）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(12, 1, 2, 'APPROVE', '该生学习认真，表现良好，推荐。', '2024-10-01 18:00:00'),
(12, 2, 1, 'APPROVE', '同意授予校级二等奖学金。', '2024-10-02 14:00:00');

-- 校级二等奖学金（application_id=13）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(13, 1, 2, 'APPROVE', '该生综合素质提升明显，推荐。', '2024-10-02 19:00:00');

-- 校级三等奖学金（application_id=15）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(15, 1, 2, 'APPROVE', '该生学习稳定，积极向上，推荐。', '2024-10-01 19:00:00'),
(15, 2, 1, 'APPROVE', '同意授予校级三等奖学金。', '2024-10-02 15:00:00');

-- 校级三等奖学金（application_id=16）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(16, 1, 2, 'APPROVE', '该生学习努力，推荐申请。', '2024-10-02 19:30:00');

-- 院系优秀奖学金（application_id=17）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(17, 1, 2, 'APPROVE', '该生在院系表现突出，推荐。', '2024-10-01 20:00:00'),
(17, 2, 1, 'APPROVE', '同意授予院系优秀奖学金。', '2024-10-02 16:00:00');

-- 院系优秀奖学金（application_id=18）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(18, 1, 2, 'APPROVE', '该生服务意识强，表现优秀，推荐。', '2024-10-02 20:00:00');

-- 创新创业奖学金（application_id=19）审批记录
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(19, 1, 2, 'APPROVE', '该生创新能力强，项目成果显著，推荐。', '2024-10-01 21:00:00'),
(19, 2, 1, 'APPROVE', '同意授予创新创业奖学金。', '2024-10-02 17:00:00');

-- 创新创业奖学金（application_id=20）审批记录（辅导员已通过）
INSERT INTO scholarship_approval (application_id, approval_level, approver_id, action, comment, approved_at) VALUES
(20, 1, 2, 'APPROVE', '该生创新意识强，积极探索，推荐。', '2024-10-02 20:30:00');

-- 3. 插入获奖记录测试数据
-- 为已完全通过审批的申请创建获奖记录

INSERT INTO scholarship_award (
    application_id, student_id, scholarship_id, academic_year, amount, awarded_at, published
) VALUES
-- 国家奖学金获奖记录（已公示）
(1, 1, 1, '2024-2025', 8000.00, '2024-10-05 10:00:00', TRUE),

-- 国家励志奖学金获奖记录（已公示）
(4, 4, 2, '2024-2025', 5000.00, '2024-10-05 11:00:00', TRUE),

-- 省政府奖学金获奖记录（已公示）
(7, 7, 3, '2024-2025', 6000.00, '2024-10-05 12:00:00', TRUE),

-- 校级一等奖学金获奖记录（已公示）
(9, 9, 4, '2024-2025', 3000.00, '2024-10-05 13:00:00', TRUE),

-- 校级二等奖学金获奖记录（已公示）
(12, 2, 5, '2024-2025', 2000.00, '2024-10-05 14:00:00', TRUE),

-- 校级三等奖学金获奖记录（已公示）
(15, 5, 6, '2024-2025', 1000.00, '2024-10-05 15:00:00', TRUE),

-- 院系优秀奖学金获奖记录（已公示）
(17, 7, 7, '2024-2025', 1500.00, '2024-10-05 16:00:00', TRUE),

-- 创新创业奖学金获奖记录（已公示）
(19, 9, 8, '2024-2025', 2500.00, '2024-10-05 17:00:00', TRUE);

-- 4. 添加历史学年的获奖记录（用于统计分析）
INSERT INTO scholarship_award (
    application_id, student_id, scholarship_id, academic_year, amount, awarded_at, published
) VALUES
-- 2023-2024学年获奖记录
(1, 1, 1, '2023-2024', 8000.00, '2023-10-05 10:00:00', TRUE),
(4, 4, 2, '2023-2024', 5000.00, '2023-10-05 11:00:00', TRUE),
(7, 7, 3, '2023-2024', 6000.00, '2023-10-05 12:00:00', TRUE),
(9, 9, 4, '2023-2024', 3000.00, '2023-10-05 13:00:00', TRUE),
(12, 2, 5, '2023-2024', 2000.00, '2023-10-05 14:00:00', TRUE),

-- 2022-2023学年获奖记录
(1, 1, 1, '2022-2023', 8000.00, '2022-10-05 10:00:00', TRUE),
(4, 4, 2, '2022-2023', 5000.00, '2022-10-05 11:00:00', TRUE),
(7, 7, 4, '2022-2023', 3000.00, '2022-10-05 12:00:00', TRUE);

-- =============================================
-- 数据验证查询
-- =============================================

-- 查询各奖学金的申请人数
-- SELECT s.name, COUNT(*) as application_count
-- FROM scholarship_application sa
-- JOIN scholarship s ON sa.scholarship_id = s.id
-- WHERE sa.academic_year = '2024-2025'
-- GROUP BY s.id, s.name;

-- 查询各状态的申请数量
-- SELECT status, COUNT(*) as count
-- FROM scholarship_application
-- WHERE academic_year = '2024-2025'
-- GROUP BY status;

-- 查询已公示的获奖名单
-- SELECT sa.student_id, s.name as student_name, sch.name as scholarship_name,
--        sa.amount, sa.awarded_at
-- FROM scholarship_award sa
-- JOIN student s ON sa.student_id = s.id
-- JOIN scholarship sch ON sa.scholarship_id = sch.id
-- WHERE sa.academic_year = '2024-2025' AND sa.published = TRUE
-- ORDER BY sa.awarded_at;

