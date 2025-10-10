-- =====================================================
-- 为Student表添加status字段
-- =====================================================

-- 添加学生状态字段
ALTER TABLE student
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '学生状态：ACTIVE-在读, SUSPENDED-休学, WITHDRAWN-退学, GRADUATED-毕业'
AFTER email;

-- 添加索引
CREATE INDEX idx_student_status ON student(status);

-- 更新已存在的数据，将所有学生状态设置为在读
UPDATE student SET status = 'ACTIVE' WHERE status IS NULL OR status = '';

