-- 为student_credit_summary表添加GPA字段
ALTER TABLE student_credit_summary 
ADD COLUMN gpa DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均绩点（0.00-4.00）' 
AFTER practical_credits;

-- 更新现有记录的GPA（根据已完成课程的成绩计算）
-- 注：这个计算会在应用启动后由Java代码自动更新，这里先设为0
UPDATE student_credit_summary SET gpa = 0.00 WHERE gpa IS NULL;

