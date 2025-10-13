-- =====================================================
-- 学籍异动管理模块测试数据
-- =====================================================

-- 测试数据：学籍异动申请

-- 1. 休学申请（待审批）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(1, 'SUSPENSION', '因身体健康原因需要休学治疗。', '2024-09-01', '2025-08-31', '/uploads/medical_certificate_001.pdf', 'PENDING', 2, 1, 0, '2024-08-15 10:00:00', '2024-08-15 10:00:00');

-- 2. 复学申请（已批准）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(2, 'RESUMPTION', '休学期满，身体状况良好，申请复学。', '2024-09-01', NULL, '/uploads/health_certificate_001.pdf', 'APPROVED', NULL, 3, 0, '2024-08-10 09:00:00', '2024-08-20 15:00:00');

-- 3. 转专业申请（待审批）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, target_major_id, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(3, 'TRANSFER', '对目标专业有浓厚兴趣，且成绩优异，希望转入软件工程专业。', NULL, NULL, 2, '/uploads/transcript_001.pdf', 'PENDING', 2, 1, 0, '2024-08-12 14:30:00', '2024-08-12 14:30:00');

-- 4. 退学申请（已批准）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(4, 'WITHDRAWAL', '家庭经济困难，无法继续学业。', '2024-08-30', NULL, '/uploads/family_proof_001.pdf', 'APPROVED', NULL, 3, 0, '2024-08-05 11:00:00', '2024-08-25 16:00:00');

-- 5. 休学申请（已拒绝）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(5, 'SUSPENSION', '想要出国旅游一年。', '2024-09-01', '2025-08-31', NULL, 'REJECTED', NULL, 1, 0, '2024-08-08 16:00:00', '2024-08-09 10:00:00');

-- 6. 转专业申请（已批准）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, target_major_id, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(6, 'TRANSFER', '对计算机科学有强烈兴趣，且已自学相关课程。', NULL, NULL, 1, '/uploads/self_study_proof_001.pdf', 'APPROVED', NULL, 3, 0, '2024-07-20 10:00:00', '2024-08-15 14:00:00');

-- 7. 复学申请（待审批）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(7, 'RESUMPTION', '休学期满，申请复学继续学业。', '2024-09-01', NULL, NULL, 'PENDING', 2, 1, 0, '2024-08-18 13:00:00', '2024-08-18 13:00:00');

-- 8. 休学申请（已取消）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(8, 'SUSPENSION', '因个人原因需要休学。', '2024-09-01', '2025-08-31', NULL, 'CANCELLED', NULL, 1, 0, '2024-08-03 09:00:00', '2024-08-04 11:00:00');

-- 9. 转专业申请（待审批 - 院系级别）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, target_major_id, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(9, 'TRANSFER', '发现自己更适合数据科学专业，申请转专业。', NULL, NULL, 3, '/uploads/interest_statement_001.pdf', 'PENDING', 3, 2, 0, '2024-08-14 15:00:00', '2024-08-19 10:00:00');

-- 10. 退学申请（待审批）
INSERT INTO student_status_change (student_id, type, reason, start_date, end_date, attachment_url, status, current_approver_id, approval_level, deleted, created_at, updated_at) VALUES
(10, 'WITHDRAWAL', '已找到工作机会，决定提前就业。', '2024-09-01', NULL, '/uploads/offer_letter_001.pdf', 'PENDING', 2, 1, 0, '2024-08-16 10:30:00', '2024-08-16 10:30:00');


-- 测试数据：审批记录

-- 复学申请（ID=2）的审批记录
INSERT INTO status_change_approval (status_change_id, approval_level, approver_id, action, comment, approved_at, deleted, created_at, updated_at) VALUES
(2, 1, 2, 'APPROVE', '该生休学期满，身体状况良好，同意复学。', '2024-08-12 10:00:00', 0, '2024-08-12 10:00:00', '2024-08-12 10:00:00'),
(2, 2, 3, 'APPROVE', '审核通过，同意该生复学。', '2024-08-15 14:00:00', 0, '2024-08-15 14:00:00', '2024-08-15 14:00:00'),
(2, 3, 1, 'APPROVE', '批准复学申请，请办理相关手续。', '2024-08-20 15:00:00', 0, '2024-08-20 15:00:00', '2024-08-20 15:00:00');

-- 退学申请（ID=4）的审批记录
INSERT INTO status_change_approval (status_change_id, approval_level, approver_id, action, comment, approved_at, deleted, created_at, updated_at) VALUES
(4, 1, 2, 'APPROVE', '了解学生家庭情况，同意退学申请。', '2024-08-10 11:00:00', 0, '2024-08-10 11:00:00', '2024-08-10 11:00:00'),
(4, 2, 3, 'APPROVE', '同意退学申请。', '2024-08-18 10:00:00', 0, '2024-08-18 10:00:00', '2024-08-18 10:00:00'),
(4, 3, 1, 'APPROVE', '批准退学申请。', '2024-08-25 16:00:00', 0, '2024-08-25 16:00:00', '2024-08-25 16:00:00');

-- 休学申请（ID=5）的审批记录（已拒绝）
INSERT INTO status_change_approval (status_change_id, approval_level, approver_id, action, comment, approved_at, deleted, created_at, updated_at) VALUES
(5, 1, 2, 'REJECT', '休学理由不充分，旅游不属于休学正当理由。', '2024-08-09 10:00:00', 0, '2024-08-09 10:00:00', '2024-08-09 10:00:00');

-- 转专业申请（ID=6）的审批记录
INSERT INTO status_change_approval (status_change_id, approval_level, approver_id, action, comment, approved_at, deleted, created_at, updated_at) VALUES
(6, 1, 2, 'APPROVE', '该生学习成绩优秀，同意转专业申请。', '2024-07-25 10:00:00', 0, '2024-07-25 10:00:00', '2024-07-25 10:00:00'),
(6, 2, 3, 'APPROVE', '该生符合转专业条件，同意转入计算机科学专业。', '2024-08-05 14:00:00', 0, '2024-08-05 14:00:00', '2024-08-05 14:00:00'),
(6, 3, 1, 'APPROVE', '批准转专业申请。', '2024-08-15 14:00:00', 0, '2024-08-15 14:00:00', '2024-08-15 14:00:00');

-- 转专业申请（ID=9）的审批记录（辅导员已批准，等待院系审批）
INSERT INTO status_change_approval (status_change_id, approval_level, approver_id, action, comment, approved_at, deleted, created_at, updated_at) VALUES
(9, 1, 2, 'APPROVE', '该生成绩良好，同意转专业申请。', '2024-08-19 10:00:00', 0, '2024-08-19 10:00:00', '2024-08-19 10:00:00');

