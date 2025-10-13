-- =====================================================
-- 添加处分审批状态字段
-- =====================================================

-- 添加审批状态字段
ALTER TABLE student_discipline 
ADD COLUMN approval_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝' AFTER status;

-- 添加审批意见字段
ALTER TABLE student_discipline 
ADD COLUMN approval_comment TEXT COMMENT '审批意见' AFTER approved_at;

-- 更新已有数据：如果有审批人，则设为已批准
UPDATE student_discipline 
SET approval_status = 'APPROVED' 
WHERE approver_id IS NOT NULL;

-- 添加索引
CREATE INDEX idx_approval_status ON student_discipline(approval_status);

