-- 添加grade_point（绩点）字段到grade表
-- grade_point的计算规则：
-- 90-100分 → 4.0
-- 85-89分 → 3.7
-- 80-84分 → 3.3
-- 75-79分 → 3.0
-- 70-74分 → 2.7
-- 65-69分 → 2.3
-- 60-64分 → 2.0
-- 60分以下 → 0.0

-- 1. 添加grade_point字段
ALTER TABLE grade ADD COLUMN grade_point DECIMAL(3,2) DEFAULT NULL COMMENT '绩点(0.00-4.00)';

-- 2. 根据total_score计算并更新grade_point
UPDATE grade 
SET grade_point = CASE
    WHEN total_score >= 90 THEN 4.0
    WHEN total_score >= 85 THEN 3.7
    WHEN total_score >= 80 THEN 3.3
    WHEN total_score >= 75 THEN 3.0
    WHEN total_score >= 70 THEN 2.7
    WHEN total_score >= 65 THEN 2.3
    WHEN total_score >= 60 THEN 2.0
    ELSE 0.0
END
WHERE total_score IS NOT NULL;

-- 3. 添加索引优化查询
CREATE INDEX idx_grade_status ON grade(status);

-- 4. 添加触发器：在插入或更新成绩时自动计算grade_point
DELIMITER //

CREATE TRIGGER before_grade_insert_calculate_grade_point
BEFORE INSERT ON grade
FOR EACH ROW
BEGIN
    IF NEW.total_score IS NOT NULL THEN
        SET NEW.grade_point = CASE
            WHEN NEW.total_score >= 90 THEN 4.0
            WHEN NEW.total_score >= 85 THEN 3.7
            WHEN NEW.total_score >= 80 THEN 3.3
            WHEN NEW.total_score >= 75 THEN 3.0
            WHEN NEW.total_score >= 70 THEN 2.7
            WHEN NEW.total_score >= 65 THEN 2.3
            WHEN NEW.total_score >= 60 THEN 2.0
            ELSE 0.0
        END;
    END IF;
END//

CREATE TRIGGER before_grade_update_calculate_grade_point
BEFORE UPDATE ON grade
FOR EACH ROW
BEGIN
    IF NEW.total_score IS NOT NULL THEN
        SET NEW.grade_point = CASE
            WHEN NEW.total_score >= 90 THEN 4.0
            WHEN NEW.total_score >= 85 THEN 3.7
            WHEN NEW.total_score >= 80 THEN 3.3
            WHEN NEW.total_score >= 75 THEN 3.0
            WHEN NEW.total_score >= 70 THEN 2.7
            WHEN NEW.total_score >= 65 THEN 2.3
            WHEN NEW.total_score >= 60 THEN 2.0
            ELSE 0.0
        END;
    END IF;
END//

DELIMITER ;

