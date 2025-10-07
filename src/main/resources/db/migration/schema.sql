-- 大学教务管理系统数据库初始化脚本
-- MySQL 8.0+

-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE academic_system;

-- 删除已存在的表（开发环境使用，生产环境请注释）
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS system_config;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS grade;
DROP TABLE IF EXISTS course_selection;
DROP TABLE IF EXISTS course_offering;
DROP TABLE IF EXISTS semester;
DROP TABLE IF EXISTS course_prerequisite;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS major;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS sys_user;

-- ============================================================================
-- 1. 用户表 (sys_user)
-- ============================================================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL COMMENT '角色',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    first_login BOOLEAN DEFAULT TRUE COMMENT '是否首次登录',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- 2. 院系表 (department)
-- ============================================================================
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '院系代码',
    name VARCHAR(100) NOT NULL COMMENT '院系名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院系表';

-- ============================================================================
-- 3. 专业表 (major)
-- ============================================================================
CREATE TABLE major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '专业代码',
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    department_id BIGINT NOT NULL COMMENT '所属院系ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES department(id),
    INDEX idx_code (code),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业表';

-- ============================================================================
-- 4. 学生表 (student)
-- ============================================================================
CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    student_no VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    enrollment_year INT NOT NULL COMMENT '入学年份',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    class_name VARCHAR(50) COMMENT '班级',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（软删除）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (major_id) REFERENCES major(id),
    INDEX idx_student_no (student_no),
    INDEX idx_major (major_id),
    INDEX idx_enrollment_year (enrollment_year),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ============================================================================
-- 5. 教师表 (teacher)
-- ============================================================================
CREATE TABLE teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    teacher_no VARCHAR(20) UNIQUE NOT NULL COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
    title VARCHAR(50) COMMENT '职称',
    department_id BIGINT NOT NULL COMMENT '所属院系ID',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (department_id) REFERENCES department(id),
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

-- ============================================================================
-- 6. 课程表 (course)
-- ============================================================================
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_no VARCHAR(20) UNIQUE NOT NULL COMMENT '课程编号',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credits INT NOT NULL COMMENT '学分',
    hours INT NOT NULL COMMENT '学时',
    type ENUM('REQUIRED', 'ELECTIVE', 'PUBLIC') NOT NULL COMMENT '课程类型：必修、选修、公共',
    department_id BIGINT NOT NULL COMMENT '开课院系ID',
    description TEXT COMMENT '课程简介',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES department(id),
    INDEX idx_course_no (course_no),
    INDEX idx_type (type),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ============================================================================
-- 7. 先修课程表 (course_prerequisite)
-- ============================================================================
CREATE TABLE course_prerequisite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    prerequisite_course_id BIGINT NOT NULL COMMENT '先修课程ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (prerequisite_course_id) REFERENCES course(id),
    UNIQUE KEY uk_course_prerequisite (course_id, prerequisite_course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='先修课程表';

-- ============================================================================
-- 8. 学期表 (semester)
-- ============================================================================
CREATE TABLE semester (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年，如：2024-2025',
    semester_type TINYINT NOT NULL COMMENT '学期类型：1-春季学期 2-秋季学期',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    course_selection_start DATETIME NOT NULL COMMENT '选课开始时间',
    course_selection_end DATETIME NOT NULL COMMENT '选课结束时间',
    active BOOLEAN DEFAULT FALSE COMMENT '是否为当前学期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_active (active),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期表';

-- ============================================================================
-- 9. 开课计划表 (course_offering)
-- ============================================================================
CREATE TABLE course_offering (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester_id BIGINT NOT NULL COMMENT '学期ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    schedule JSON COMMENT '上课时间（JSON格式）：[{day:1,period:"1-2",weeks:[1,2,3...],location:"教学楼A101"}]',
    location VARCHAR(100) COMMENT '上课地点',
    capacity INT NOT NULL COMMENT '容量上限',
    enrolled INT DEFAULT 0 COMMENT '已选人数',
    status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED') DEFAULT 'DRAFT' COMMENT '状态：草稿、已发布、已取消',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (semester_id) REFERENCES semester(id),
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    INDEX idx_semester (semester_id),
    INDEX idx_course (course_id),
    INDEX idx_teacher (teacher_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开课计划表';

-- ============================================================================
-- 10. 选课记录表 (course_selection)
-- ============================================================================
CREATE TABLE course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    selection_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    status ENUM('SELECTED', 'DROPPED', 'COMPLETED') DEFAULT 'SELECTED' COMMENT '状态：已选、已退、已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_offering_id) REFERENCES course_offering(id),
    UNIQUE KEY uk_student_offering (student_id, course_offering_id),
    INDEX idx_student (student_id),
    INDEX idx_offering (course_offering_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- ============================================================================
-- 11. 成绩表 (grade)
-- ============================================================================
CREATE TABLE grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_selection_id BIGINT UNIQUE NOT NULL COMMENT '选课记录ID',
    regular_score DECIMAL(5,2) COMMENT '平时成绩',
    midterm_score DECIMAL(5,2) COMMENT '期中成绩',
    final_score DECIMAL(5,2) COMMENT '期末成绩',
    total_score DECIMAL(5,2) COMMENT '总评成绩',
    status ENUM('DRAFT', 'SUBMITTED', 'PUBLISHED') DEFAULT 'DRAFT' COMMENT '状态：草稿、已提交、已公布',
    submitted_at DATETIME COMMENT '提交时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (course_selection_id) REFERENCES course_selection(id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ============================================================================
-- 12. 通知公告表 (notification)
-- ============================================================================
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type ENUM('SYSTEM', 'COURSE', 'GRADE', 'SELECTION') NOT NULL COMMENT '类型：系统、课程、成绩、选课',
    publisher_id BIGINT NOT NULL COMMENT '发布者ID',
    target_role VARCHAR(50) COMMENT '目标角色：ALL、ADMIN、TEACHER、STUDENT',
    publish_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (publisher_id) REFERENCES sys_user(id),
    INDEX idx_type (type),
    INDEX idx_publish_time (publish_time),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- ============================================================================
-- 13. 通知阅读记录表 (notification_read)
-- ============================================================================
CREATE TABLE notification_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    FOREIGN KEY (notification_id) REFERENCES notification(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    UNIQUE KEY uk_notification_user (notification_id, user_id),
    INDEX idx_notification (notification_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知阅读记录表';

-- ============================================================================
-- 14. 系统配置表 (system_config)
-- ============================================================================
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 15. 操作日志表 (operation_log)
-- ============================================================================
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) COMMENT '方法名',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    execution_time BIGINT COMMENT '执行时长（毫秒）',
    status ENUM('SUCCESS', 'FAILURE') COMMENT '状态',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================================
-- 初始化数据
-- ============================================================================

-- 插入默认院系
INSERT INTO department (code, name) VALUES
('CS', '计算机科学与技术学院'),
('MATH', '数学与统计学院'),
('PHY', '物理学院'),
('CHEM', '化学学院'),
('BIO', '生命科学学院');

-- 插入默认专业
INSERT INTO major (code, name, department_id) VALUES
('CS01', '计算机科学与技术', 1),
('CS02', '软件工程', 1),
('CS03', '人工智能', 1),
('MATH01', '数学与应用数学', 2),
('MATH02', '统计学', 2);

-- 插入默认管理员账户（密码：admin123，需要BCrypt加密）
-- 注意：实际密码应该在应用启动时通过代码生成BCrypt加密
INSERT INTO sys_user (username, password, role, first_login) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', FALSE);

-- 插入系统配置（使用点号分隔的配置键）
INSERT INTO system_config (config_key, config_value, description) VALUES
('max.credits.per.semester', '30', '每学期最大学分'),
('max.courses.per.semester', '8', '每学期最多课程数'),
('drop.deadline.days', '14', '退课截止天数'),
('login.failure.limit', '3', '登录失败次数限制'),
('account.lock.minutes', '15', '账户锁定时间（分钟）'),
('default.password', '123456', '默认密码'),
('system.name', '大学教务管理系统', '系统名称'),
('system.maintenance.mode', 'false', '系统维护模式'),
('academic.year', '2024-2025', '当前学年'),
('grade.pass.score', '60', '及格分数线'),
('selection.time.check', 'true', '是否检查选课时间');

-- 创建视图：学生详细信息视图
CREATE OR REPLACE VIEW v_student_detail AS
SELECT 
    s.id,
    s.student_no,
    s.name,
    s.gender,
    s.birth_date,
    s.enrollment_year,
    s.class_name,
    s.phone,
    s.email,
    m.name AS major_name,
    m.code AS major_code,
    d.name AS department_name,
    d.code AS department_code,
    u.username,
    u.enabled,
    s.deleted,
    s.created_at,
    s.updated_at
FROM student s
LEFT JOIN major m ON s.major_id = m.id
LEFT JOIN department d ON m.department_id = d.id
LEFT JOIN sys_user u ON s.user_id = u.id;

-- 创建视图：教师详细信息视图
CREATE OR REPLACE VIEW v_teacher_detail AS
SELECT 
    t.id,
    t.teacher_no,
    t.name,
    t.gender,
    t.title,
    t.phone,
    t.email,
    d.name AS department_name,
    d.code AS department_code,
    u.username,
    u.enabled,
    t.created_at,
    t.updated_at
FROM teacher t
LEFT JOIN department d ON t.department_id = d.id
LEFT JOIN sys_user u ON t.user_id = u.id;

-- 创建视图：课程详细信息视图
CREATE OR REPLACE VIEW v_course_detail AS
SELECT 
    c.id,
    c.course_no,
    c.name,
    c.credits,
    c.hours,
    c.type,
    c.description,
    d.name AS department_name,
    d.code AS department_code,
    c.created_at,
    c.updated_at
FROM course c
LEFT JOIN department d ON c.department_id = d.id;

COMMIT;

