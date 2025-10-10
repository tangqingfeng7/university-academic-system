-- =====================================================
-- 学籍异动管理模块测试数据
-- 快速插入脚本（用于测试）
-- 执行此脚本前请确保已有学生、用户等基础数据
-- =====================================================

-- 清理旧数据（可选）
-- DELETE FROM status_change_approval WHERE deleted = 0;
-- DELETE FROM student_status_change WHERE deleted = 0;

-- =====================================================
-- 1. 学籍异动申请测试数据
-- =====================================================

-- 1.1 休学申请（待审批 - 第1级）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date, 
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    1,  -- 学生ID（张三）
    'SUSPENSION',
    '因身体健康原因需要休学治疗，医院建议休息半年。已附上医院诊断证明和病历资料。',
    '2024-09-01',
    '2025-02-28',
    '/uploads/medical-certificate-001.pdf',
    'PENDING',
    2,  -- 当前审批人ID（教师T001）
    1,  -- 第1级审批（辅导员）
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    0,
    0,
    NOW(),
    NOW()
);

-- 1.2 转专业申请（待审批 - 第1级）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date, target_major_id,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    2,  -- 学生ID（李四）
    'TRANSFER',
    '对计算机科学专业更感兴趣，且相关课程成绩优秀。希望能够转入计算机科学与技术专业深造。',
    NULL,
    NULL,
    2,  -- 目标专业ID（软件工程）
    '/uploads/transcript-002.pdf',
    'PENDING',
    2,  -- 当前审批人ID
    1,  -- 第1级审批
    DATE_ADD(NOW(), INTERVAL 14 DAY),
    0,
    0,
    NOW(),
    NOW()
);

-- 1.3 复学申请（待审批 - 第1级）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    3,  -- 学生ID（王五）
    'RESUMPTION',
    '休学期满，身体状况已恢复，申请复学继续学业。附上医院康复证明。',
    '2024-09-01',
    NULL,
    '/uploads/health-certificate-003.pdf',
    'PENDING',
    2,  -- 当前审批人ID
    1,  -- 第1级审批
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    0,
    0,
    NOW(),
    NOW()
);

-- 1.4 退学申请（待审批 - 第1级）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    4,  -- 学生ID（赵六）
    'WITHDRAWAL',
    '已获得国外大学录取通知书，决定出国深造。附上录取通知书复印件。',
    '2024-09-01',
    NULL,
    '/uploads/admission-letter-004.pdf',
    'PENDING',
    2,  -- 当前审批人ID
    1,  -- 第1级审批
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    0,
    0,
    NOW(),
    NOW()
);

-- 1.5 休学申请（已批准）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    5,  -- 学生ID（孙七）
    'SUSPENSION',
    '因家庭原因需要休学一年处理家事。',
    '2024-03-01',
    '2025-02-28',
    NULL,
    'APPROVED',
    NULL,
    3,  -- 已完成第3级审批
    NULL,
    0,
    0,
    DATE_SUB(NOW(), INTERVAL 90 DAY),
    DATE_SUB(NOW(), INTERVAL 80 DAY)
);

-- 1.6 转专业申请（已拒绝）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date, target_major_id,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    6,  -- 学生ID（周八）
    'TRANSFER',
    '希望转入金融专业学习。',
    NULL,
    NULL,
    4,  -- 目标专业ID（信息管理与信息系统）
    NULL,
    'REJECTED',
    NULL,
    1,  -- 在第1级就被拒绝
    NULL,
    0,
    0,
    DATE_SUB(NOW(), INTERVAL 60 DAY),
    DATE_SUB(NOW(), INTERVAL 55 DAY)
);

-- 1.7 复学申请（已批准）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    7,  -- 学生ID（吴九）
    'RESUMPTION',
    '休学期满，申请复学。',
    '2024-09-01',
    NULL,
    NULL,
    'APPROVED',
    NULL,
    3,
    NULL,
    0,
    0,
    DATE_SUB(NOW(), INTERVAL 30 DAY),
    DATE_SUB(NOW(), INTERVAL 20 DAY)
);

-- 1.8 休学申请（已取消）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted, created_at, updated_at
) VALUES (
    8,  -- 学生ID（郑十）
    'SUSPENSION',
    '因个人原因申请休学。',
    '2024-09-01',
    '2025-08-31',
    NULL,
    'CANCELLED',
    NULL,
    1,
    NULL,
    0,
    0,
    DATE_SUB(NOW(), INTERVAL 45 DAY),
    DATE_SUB(NOW(), INTERVAL 44 DAY)
);

-- =====================================================
-- 2. 审批记录测试数据
-- =====================================================

-- 2.1 已批准休学申请（ID=5）的审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted, created_at, updated_at
) VALUES 
(5, 1, 2, 'APPROVE', '了解学生家庭情况，同意休学申请。', DATE_SUB(NOW(), INTERVAL 89 DAY), 0, DATE_SUB(NOW(), INTERVAL 89 DAY), DATE_SUB(NOW(), INTERVAL 89 DAY)),
(5, 2, 1, 'APPROVE', '同意该生休学申请。', DATE_SUB(NOW(), INTERVAL 85 DAY), 0, DATE_SUB(NOW(), INTERVAL 85 DAY), DATE_SUB(NOW(), INTERVAL 85 DAY)),
(5, 3, 1, 'APPROVE', '批准休学申请，请办理相关手续。', DATE_SUB(NOW(), INTERVAL 80 DAY), 0, DATE_SUB(NOW(), INTERVAL 80 DAY), DATE_SUB(NOW(), INTERVAL 80 DAY));

-- 2.2 已拒绝转专业申请（ID=6）的审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted, created_at, updated_at
) VALUES 
(6, 1, 2, 'REJECT', '学生现专业成绩不理想，不建议转专业。建议先提升当前专业成绩。', DATE_SUB(NOW(), INTERVAL 55 DAY), 0, DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 55 DAY));

-- 2.3 已批准复学申请（ID=7）的审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted, created_at, updated_at
) VALUES 
(7, 1, 2, 'APPROVE', '该生休学期满，同意复学。', DATE_SUB(NOW(), INTERVAL 29 DAY), 0, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
(7, 2, 1, 'APPROVE', '审核通过，同意复学。', DATE_SUB(NOW(), INTERVAL 25 DAY), 0, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
(7, 3, 1, 'APPROVE', '批准复学申请。', DATE_SUB(NOW(), INTERVAL 20 DAY), 0, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY));

-- =====================================================
-- 执行完成提示
-- =====================================================
-- 查询验证数据
SELECT '学籍异动测试数据插入完成！' AS message;
SELECT COUNT(*) AS total_applications FROM student_status_change WHERE deleted = 0;
SELECT status, COUNT(*) AS count FROM student_status_change WHERE deleted = 0 GROUP BY status;

