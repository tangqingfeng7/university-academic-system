-- =====================================================
-- 学籍异动管理模块测试数据
-- 注意：此脚本仅用于开发测试环境，不应在生产环境执行
-- 执行前提：已有user、student、major等基础数据
-- =====================================================

-- =====================================================
-- 1. 审批人配置数据
-- =====================================================
-- 为三个审批级别配置审批人（假设已有教师和管理员用户）
-- 请根据实际的用户ID进行调整

-- 第1级审批人（辅导员）- 假设用户ID为2和3的是教师
INSERT INTO approval_level_approver (approval_level, user_id, priority, pending_count, enabled) 
VALUES 
(1, 2, 1, 0, 1),  -- 第一个辅导员
(1, 3, 2, 0, 1);  -- 第二个辅导员（备用）

-- 第2级审批人（院系）- 假设用户ID为1是管理员
INSERT INTO approval_level_approver (approval_level, user_id, priority, pending_count, enabled) 
VALUES 
(2, 1, 1, 0, 1);  -- 院系管理员

-- 第3级审批人（教务处）- 假设用户ID为1是管理员
INSERT INTO approval_level_approver (approval_level, user_id, priority, pending_count, enabled) 
VALUES 
(3, 1, 1, 0, 1);  -- 教务处管理员

-- =====================================================
-- 2. 学籍异动申请测试数据
-- =====================================================

-- 2.1 休学申请（待审批）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date, 
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    1,  -- 假设学生ID为1
    'SUSPENSION',
    '因身体健康原因需要休学治疗，医院建议休息半年。已附上医院诊断证明和病历资料。',
    '2024-09-01',
    '2025-02-28',
    '/uploads/status-change/medical-certificate-001.pdf',
    'PENDING',
    2,  -- 当前审批人（辅导员）
    1,  -- 第1级审批
    DATE_ADD(NOW(), INTERVAL 7 DAY),  -- 7天后截止
    0,
    0
);

-- 2.2 转专业申请（审批中，已通过第1级）
INSERT INTO student_status_change (
    student_id, type, reason, target_major_id,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    2,  -- 假设学生ID为2
    'TRANSFER',
    '对计算机科学专业更感兴趣，且相关课程成绩优秀。希望能够转入计算机科学与技术专业深造。已附上成绩单和个人学习计划。',
    2,  -- 假设目标专业ID为2（计算机科学与技术）
    '/uploads/status-change/transcript-002.pdf',
    'PENDING',
    1,  -- 当前审批人（院系管理员）
    2,  -- 第2级审批
    DATE_ADD(NOW(), INTERVAL 14 DAY),  -- 14天后截止（转专业时限更长）
    0,
    0
);

-- 添加第1级审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted
) VALUES (
    2,  -- 对应上面的转专业申请
    1,
    2,  -- 辅导员
    'APPROVE',
    '该学生学习态度认真，成绩优秀，同意其转专业申请。',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    0
);

-- 2.3 复学申请（已批准）
INSERT INTO student_status_change (
    student_id, type, reason, start_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    3,  -- 假设学生ID为3
    'RESUMPTION',
    '休学期满，身体已康复，申请复学继续学业。已附上医院康复证明。',
    '2024-09-01',
    '/uploads/status-change/recovery-certificate-003.pdf',
    'APPROVED',
    NULL,  -- 已完成审批，无当前审批人
    3,  -- 已完成所有3级审批
    DATE_SUB(NOW(), INTERVAL 3 DAY),  -- 已过截止时间
    0,
    0
);

-- 添加完整的审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted
) VALUES 
(3, 1, 2, 'APPROVE', '学生身体已恢复，同意复学。', DATE_SUB(NOW(), INTERVAL 8 DAY), 0),
(3, 2, 1, 'APPROVE', '符合复学条件，同意复学申请。', DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(3, 3, 1, 'APPROVE', '审核通过，同意该生复学。', DATE_SUB(NOW(), INTERVAL 3 DAY), 0);

-- 2.4 退学申请（已拒绝）
INSERT INTO student_status_change (
    student_id, type, reason,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    4,  -- 假设学生ID为4
    'WITHDRAWAL',
    '因家庭经济困难，无法继续学业，申请退学。',
    '/uploads/status-change/family-proof-004.pdf',
    'REJECTED',
    NULL,
    1,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    0,
    0
);

-- 添加拒绝审批记录
INSERT INTO status_change_approval (
    status_change_id, approval_level, approver_id, action, 
    comment, approved_at, deleted
) VALUES (
    4,
    1,
    2,
    'REJECT',
    '建议先申请助学贷款和助学金，学校可以提供帮助。暂不建议退学。',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    0
);

-- 2.5 休学申请（超时）
INSERT INTO student_status_change (
    student_id, type, reason, start_date, end_date,
    attachment_url, status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    5,  -- 假设学生ID为5
    'SUSPENSION',
    '因出国交流学习需要休学一年。',
    '2024-09-01',
    '2025-08-31',
    '/uploads/status-change/exchange-program-005.pdf',
    'PENDING',
    2,
    1,
    DATE_SUB(NOW(), INTERVAL 2 DAY),  -- 已超过截止时间2天
    1,  -- 标记为超时
    0
);

-- 2.6 转专业申请（已取消）
INSERT INTO student_status_change (
    student_id, type, reason, target_major_id,
    status, current_approver_id, approval_level,
    deadline, is_overdue, deleted
) VALUES (
    6,  -- 假设学生ID为6
    'TRANSFER',
    '希望转入软件工程专业。（学生已主动撤销此申请）',
    3,  -- 假设目标专业ID为3
    'CANCELLED',
    NULL,
    1,
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    0,
    0
);

-- =====================================================
-- 3. 用户通知测试数据
-- =====================================================

-- 给学生1发送审批进度通知
INSERT INTO user_notification (
    user_id, title, content, type, reference_id, reference_type,
    is_read, sent_time
) VALUES (
    1,  -- 假设学生1的用户ID也是1
    '休学申请已提交',
    '您的休学申请（编号：1）已成功提交，当前等待辅导员审批。请留意审批进度。',
    'STATUS_CHANGE',
    1,
    'STATUS_CHANGE',
    0,
    NOW()
);

-- 给审批人发送待审批通知
INSERT INTO user_notification (
    user_id, title, content, type, reference_id, reference_type,
    is_read, sent_time
) VALUES (
    2,  -- 辅导员用户ID
    '新的学籍异动申请待审批',
    '学生张三提交了休学申请（编号：1），请及时处理。',
    'STATUS_CHANGE',
    1,
    'STATUS_CHANGE',
    0,
    NOW()
);

-- =====================================================
-- 说明
-- =====================================================
-- 以上测试数据涵盖了各种场景：
-- 1. 待审批的休学申请
-- 2. 审批中的转专业申请（已通过第1级）
-- 3. 已批准的复学申请（完整审批流程）
-- 4. 已拒绝的退学申请
-- 5. 超时的休学申请
-- 6. 已取消的转专业申请
--
-- 注意事项：
-- 1. 请根据实际的user、student、major表数据调整ID
-- 2. 文件路径仅为示例，实际使用时需要上传真实文件
-- 3. 审批人配置需要根据实际的用户角色和权限设置
-- 4. 此脚本适合在开发和测试环境使用，不要在生产环境执行

