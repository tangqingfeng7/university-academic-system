-- ============================================================================
-- 教学评价模块数据库迁移脚本
-- Version: V10
-- Description: 创建教学评价相关表（课程评价、教师评价、评价周期）
-- Author: Academic System Team
-- Date: 2025-01-09
-- ============================================================================

USE academic_system;

-- ============================================================================
-- 1. 评价周期表 (evaluation_period)
-- ============================================================================
CREATE TABLE IF NOT EXISTS evaluation_period (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester_id BIGINT UNIQUE NOT NULL COMMENT '学期ID（一个学期只能有一个评价周期）',
    start_time DATETIME NOT NULL COMMENT '评价开始时间',
    end_time DATETIME NOT NULL COMMENT '评价结束时间',
    description VARCHAR(500) COMMENT '评价周期描述',
    is_active BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否激活（同时只能有一个激活的周期）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT,
    INDEX idx_eval_period_semester (semester_id),
    INDEX idx_eval_period_active (is_active),
    INDEX idx_eval_period_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价周期表';

-- ============================================================================
-- 2. 课程评价表 (course_evaluation)
-- ============================================================================
CREATE TABLE IF NOT EXISTS course_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    rating INT NOT NULL COMMENT '课程评分（1-5星）',
    comment TEXT COMMENT '文字评价',
    anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否匿名评价',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '评价状态（DRAFT-草稿, SUBMITTED-已提交, REVIEWED-已审核）',
    semester_id BIGINT NOT NULL COMMENT '学期ID（冗余字段，用于快速查询）',
    flagged BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被标记（包含敏感词等）',
    moderation_note VARCHAR(500) COMMENT '审核备注（标记原因）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_offering_id) REFERENCES course_offering(id) ON DELETE CASCADE,
    FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_student_offering (student_id, course_offering_id),
    INDEX idx_course_eval_student (student_id),
    INDEX idx_course_eval_offering (course_offering_id),
    INDEX idx_course_eval_semester (semester_id),
    INDEX idx_course_eval_status (status),
    INDEX idx_course_eval_flagged (flagged),
    INDEX idx_course_eval_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程评价表';

-- ============================================================================
-- 3. 教师评价表 (teacher_evaluation)
-- ============================================================================
CREATE TABLE IF NOT EXISTS teacher_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    course_offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    teaching_rating INT NOT NULL COMMENT '教学能力评分（1-5）',
    attitude_rating INT NOT NULL COMMENT '教学态度评分（1-5）',
    content_rating INT NOT NULL COMMENT '教学内容评分（1-5）',
    comment TEXT COMMENT '文字评价',
    anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否匿名评价',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '评价状态（DRAFT-草稿, SUBMITTED-已提交, REVIEWED-已审核）',
    flagged BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被标记（包含敏感词等）',
    moderation_note VARCHAR(500) COMMENT '审核备注（标记原因）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    FOREIGN KEY (course_offering_id) REFERENCES course_offering(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_teacher_offering (student_id, teacher_id, course_offering_id),
    INDEX idx_teacher_eval_student (student_id),
    INDEX idx_teacher_eval_teacher (teacher_id),
    INDEX idx_teacher_eval_offering (course_offering_id),
    INDEX idx_teacher_eval_status (status),
    INDEX idx_teacher_eval_flagged (flagged),
    INDEX idx_teacher_eval_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师评价表';

-- ============================================================================
-- 数据校验约束
-- ============================================================================

-- 课程评价的评分必须在1-5之间
ALTER TABLE course_evaluation 
    ADD CONSTRAINT chk_course_rating CHECK (rating >= 1 AND rating <= 5);

-- 教师评价的各项评分必须在1-5之间
ALTER TABLE teacher_evaluation 
    ADD CONSTRAINT chk_teaching_rating CHECK (teaching_rating >= 1 AND teaching_rating <= 5),
    ADD CONSTRAINT chk_attitude_rating CHECK (attitude_rating >= 1 AND attitude_rating <= 5),
    ADD CONSTRAINT chk_content_rating CHECK (content_rating >= 1 AND content_rating <= 5);

-- 评价周期的结束时间必须晚于开始时间
ALTER TABLE evaluation_period
    ADD CONSTRAINT chk_period_time CHECK (end_time > start_time);

-- ============================================================================
-- 创建视图：方便查询评价统计信息
-- ============================================================================

-- 课程评价统计视图
CREATE OR REPLACE VIEW v_course_evaluation_stats AS
SELECT 
    co.id AS offering_id,
    c.id AS course_id,
    c.course_no,
    c.name AS course_name,
    t.id AS teacher_id,
    t.name AS teacher_name,
    s.id AS semester_id,
    s.academic_year,
    s.semester_type,
    COUNT(ce.id) AS total_evaluations,
    AVG(ce.rating) AS average_rating,
    SUM(CASE WHEN ce.rating = 5 THEN 1 ELSE 0 END) AS star_5_count,
    SUM(CASE WHEN ce.rating = 4 THEN 1 ELSE 0 END) AS star_4_count,
    SUM(CASE WHEN ce.rating = 3 THEN 1 ELSE 0 END) AS star_3_count,
    SUM(CASE WHEN ce.rating = 2 THEN 1 ELSE 0 END) AS star_2_count,
    SUM(CASE WHEN ce.rating = 1 THEN 1 ELSE 0 END) AS star_1_count,
    SUM(CASE WHEN ce.flagged = TRUE THEN 1 ELSE 0 END) AS flagged_count
FROM course_evaluation ce
INNER JOIN course_offering co ON ce.course_offering_id = co.id
INNER JOIN course c ON co.course_id = c.id
INNER JOIN teacher t ON co.teacher_id = t.id
INNER JOIN semester s ON co.semester_id = s.id
WHERE ce.status = 'SUBMITTED'
GROUP BY co.id, c.id, c.course_no, c.name, t.id, t.name, s.id, s.academic_year, s.semester_type;

-- 教师评价统计视图
CREATE OR REPLACE VIEW v_teacher_evaluation_stats AS
SELECT 
    t.id AS teacher_id,
    t.teacher_no,
    t.name AS teacher_name,
    t.department_id,
    d.name AS department_name,
    COUNT(te.id) AS total_evaluations,
    AVG(te.teaching_rating) AS avg_teaching_rating,
    AVG(te.attitude_rating) AS avg_attitude_rating,
    AVG(te.content_rating) AS avg_content_rating,
    AVG((te.teaching_rating + te.attitude_rating + te.content_rating) / 3.0) AS overall_rating,
    SUM(CASE WHEN te.flagged = TRUE THEN 1 ELSE 0 END) AS flagged_count
FROM teacher_evaluation te
INNER JOIN teacher t ON te.teacher_id = t.id
INNER JOIN department d ON t.department_id = d.id
WHERE te.status = 'SUBMITTED'
GROUP BY t.id, t.teacher_no, t.name, t.department_id, d.name;

-- 学期评价参与率视图
CREATE OR REPLACE VIEW v_semester_evaluation_participation AS
SELECT 
    s.id AS semester_id,
    s.academic_year,
    s.semester_type,
    COUNT(DISTINCT co.id) AS total_offerings,
    COUNT(DISTINCT cs.student_id) AS total_students,
    COUNT(DISTINCT ce.student_id) AS evaluated_students,
    COUNT(ce.id) AS total_course_evaluations,
    COUNT(te.id) AS total_teacher_evaluations,
    ROUND(COUNT(DISTINCT ce.student_id) / COUNT(DISTINCT cs.student_id) * 100, 2) AS participation_rate
FROM semester s
LEFT JOIN course_offering co ON s.id = co.semester_id
LEFT JOIN course_selection cs ON co.id = cs.offering_id AND cs.status = 'SELECTED'
LEFT JOIN course_evaluation ce ON co.id = ce.course_offering_id AND ce.status = 'SUBMITTED'
LEFT JOIN teacher_evaluation te ON co.id = te.course_offering_id AND te.status = 'SUBMITTED'
GROUP BY s.id, s.academic_year, s.semester_type;

-- ============================================================================
-- 插入初始化数据说明
-- ============================================================================
-- 实际的测试数据将在 evaluation_test_data.sql 中添加

COMMIT;

