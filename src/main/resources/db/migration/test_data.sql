-- 测试数据脚本
-- 数据库: academic_system
-- 用户: root
-- 密码: 123456

USE academic_system;

-- 注意：admin账户由DatabaseInitializer自动创建，id=1
-- 教师user_id从2开始

-- 1. 插入院系数据
INSERT INTO department (code, name, created_at, updated_at) VALUES
('CS', '计算机科学学院', NOW(), NOW()),
('MATH', '数学学院', NOW(), NOW()),
('PHYS', '物理学院', NOW(), NOW()),
('CHEM', '化学学院', NOW(), NOW()),
('BIO', '生物学院', NOW(), NOW()),
('ENG', '工程学院', NOW(), NOW()),
('ECON', '经济学院', NOW(), NOW()),
('MGMT', '管理学院', NOW(), NOW());

-- 2. 插入专业数据
INSERT INTO major (code, name, department_id, created_at, updated_at) VALUES
-- 计算机科学学院
('CS01', '计算机科学与技术', 1, NOW(), NOW()),
('CS02', '软件工程', 1, NOW(), NOW()),
('CS03', '网络工程', 1, NOW(), NOW()),
('CS04', '人工智能', 1, NOW(), NOW()),
('CS05', '数据科学与大数据技术', 1, NOW(), NOW()),
-- 数学学院
('MATH01', '数学与应用数学', 2, NOW(), NOW()),
('MATH02', '信息与计算科学', 2, NOW(), NOW()),
('MATH03', '统计学', 2, NOW(), NOW()),
-- 物理学院
('PHYS01', '物理学', 3, NOW(), NOW()),
('PHYS02', '应用物理学', 3, NOW(), NOW()),
-- 化学学院
('CHEM01', '化学', 4, NOW(), NOW()),
('CHEM02', '应用化学', 4, NOW(), NOW()),
-- 生物学院
('BIO01', '生物科学', 5, NOW(), NOW()),
('BIO02', '生物技术', 5, NOW(), NOW()),
-- 工程学院
('ENG01', '电子工程', 6, NOW(), NOW()),
('ENG02', '机械工程', 6, NOW(), NOW()),
('ENG03', '土木工程', 6, NOW(), NOW()),
-- 经济学院
('ECON01', '经济学', 7, NOW(), NOW()),
('ECON02', '金融学', 7, NOW(), NOW()),
-- 管理学院
('MGMT01', '工商管理', 8, NOW(), NOW()),
('MGMT02', '会计学', 8, NOW(), NOW());

-- 3. 插入教师数据 (50名教师)
INSERT INTO sys_user (username, password, role, enabled, first_login, created_at, updated_at) VALUES
-- 教师账户 (密码都是 teacher123)
('T001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T011', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T012', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T013', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T014', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T015', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T016', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T017', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T018', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T019', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T020', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T021', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T022', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T023', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T024', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T025', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T026', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T027', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T028', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T029', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T030', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T031', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T032', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T033', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T034', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T035', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T036', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T037', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T038', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T039', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T040', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T041', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T042', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T043', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T044', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T045', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T046', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T047', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T048', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T049', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW()),
('T050', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi', 'TEACHER', TRUE, FALSE, NOW(), NOW());

-- 获取刚插入的user_id (从2开始,因为1是admin)
INSERT INTO teacher (user_id, teacher_no, name, gender, title, department_id, phone, email, created_at, updated_at) VALUES
(2, 'T001', '张伟', 'MALE', '教授', 1, '13800001001', 'zhangwei@university.edu', NOW(), NOW()),
(3, 'T002', '李娜', 'FEMALE', '副教授', 1, '13800001002', 'lina@university.edu', NOW(), NOW()),
(4, 'T003', '王芳', 'FEMALE', '讲师', 1, '13800001003', 'wangfang@university.edu', NOW(), NOW()),
(5, 'T004', '刘强', 'MALE', '教授', 1, '13800001004', 'liuqiang@university.edu', NOW(), NOW()),
(6, 'T005', '陈静', 'FEMALE', '副教授', 1, '13800001005', 'chenjing@university.edu', NOW(), NOW()),
(7, 'T006', '杨明', 'MALE', '讲师', 1, '13800001006', 'yangming@university.edu', NOW(), NOW()),
(8, 'T007', '赵丽', 'FEMALE', '教授', 2, '13800001007', 'zhaoli@university.edu', NOW(), NOW()),
(9, 'T008', '孙鹏', 'MALE', '副教授', 2, '13800001008', 'sunpeng@university.edu', NOW(), NOW()),
(10, 'T009', '周敏', 'FEMALE', '讲师', 2, '13800001009', 'zhoumin@university.edu', NOW(), NOW()),
(11, 'T010', '吴涛', 'MALE', '教授', 2, '13800001010', 'wutao@university.edu', NOW(), NOW()),
(12, 'T011', '郑红', 'FEMALE', '副教授', 3, '13800001011', 'zhenghong@university.edu', NOW(), NOW()),
(13, 'T012', '王磊', 'MALE', '讲师', 3, '13800001012', 'wanglei@university.edu', NOW(), NOW()),
(14, 'T013', '冯娟', 'FEMALE', '教授', 3, '13800001013', 'fengjuan@university.edu', NOW(), NOW()),
(15, 'T014', '陈浩', 'MALE', '副教授', 4, '13800001014', 'chenhao@university.edu', NOW(), NOW()),
(16, 'T015', '李梅', 'FEMALE', '讲师', 4, '13800001015', 'limei@university.edu', NOW(), NOW()),
(17, 'T016', '张勇', 'MALE', '教授', 4, '13800001016', 'zhangyong@university.edu', NOW(), NOW()),
(18, 'T017', '刘丽', 'FEMALE', '副教授', 5, '13800001017', 'liuli@university.edu', NOW(), NOW()),
(19, 'T018', '黄伟', 'MALE', '讲师', 5, '13800001018', 'huangwei@university.edu', NOW(), NOW()),
(20, 'T019', '林娟', 'FEMALE', '教授', 5, '13800001019', 'linjuan@university.edu', NOW(), NOW()),
(21, 'T020', '徐鹏', 'MALE', '副教授', 6, '13800001020', 'xupeng@university.edu', NOW(), NOW()),
(22, 'T021', '何敏', 'FEMALE', '讲师', 6, '13800001021', 'hemin@university.edu', NOW(), NOW()),
(23, 'T022', '朱涛', 'MALE', '教授', 6, '13800001022', 'zhutao@university.edu', NOW(), NOW()),
(24, 'T023', '高红', 'FEMALE', '副教授', 6, '13800001023', 'gaohong@university.edu', NOW(), NOW()),
(25, 'T024', '郭磊', 'MALE', '讲师', 6, '13800001024', 'guolei@university.edu', NOW(), NOW()),
(26, 'T025', '罗娟', 'FEMALE', '教授', 7, '13800001025', 'luojuan@university.edu', NOW(), NOW()),
(27, 'T026', '宋浩', 'MALE', '副教授', 7, '13800001026', 'songhao@university.edu', NOW(), NOW()),
(28, 'T027', '梁丽', 'FEMALE', '讲师', 7, '13800001027', 'liangli@university.edu', NOW(), NOW()),
(29, 'T028', '韩伟', 'MALE', '教授', 7, '13800001028', 'hanwei@university.edu', NOW(), NOW()),
(30, 'T029', '唐娟', 'FEMALE', '副教授', 8, '13800001029', 'tangjuan@university.edu', NOW(), NOW()),
(31, 'T030', '冯鹏', 'MALE', '讲师', 8, '13800001030', 'fengpeng@university.edu', NOW(), NOW()),
(32, 'T031', '蔡敏', 'FEMALE', '教授', 8, '13800001031', 'caimin@university.edu', NOW(), NOW()),
(33, 'T032', '曹涛', 'MALE', '副教授', 8, '13800001032', 'caotao@university.edu', NOW(), NOW()),
(34, 'T033', '彭红', 'FEMALE', '讲师', 1, '13800001033', 'penghong@university.edu', NOW(), NOW()),
(35, 'T034', '吕磊', 'MALE', '教授', 1, '13800001034', 'lvlei@university.edu', NOW(), NOW()),
(36, 'T035', '常娟', 'FEMALE', '副教授', 2, '13800001035', 'changjuan@university.edu', NOW(), NOW()),
(37, 'T036', '马浩', 'MALE', '讲师', 2, '13800001036', 'mahao@university.edu', NOW(), NOW()),
(38, 'T037', '苗丽', 'FEMALE', '教授', 3, '13800001037', 'miaoli@university.edu', NOW(), NOW()),
(39, 'T038', '范伟', 'MALE', '副教授', 3, '13800001038', 'fanwei@university.edu', NOW(), NOW()),
(40, 'T039', '邹娟', 'FEMALE', '讲师', 4, '13800001039', 'zoujuan@university.edu', NOW(), NOW()),
(41, 'T040', '石鹏', 'MALE', '教授', 4, '13800001040', 'shipeng@university.edu', NOW(), NOW()),
(42, 'T041', '金敏', 'FEMALE', '副教授', 5, '13800001041', 'jinmin@university.edu', NOW(), NOW()),
(43, 'T042', '段涛', 'MALE', '讲师', 5, '13800001042', 'duantao@university.edu', NOW(), NOW()),
(44, 'T043', '姚红', 'FEMALE', '教授', 6, '13800001043', 'yaohong@university.edu', NOW(), NOW()),
(45, 'T044', '谭磊', 'MALE', '副教授', 6, '13800001044', 'tanlei@university.edu', NOW(), NOW()),
(46, 'T045', '盛娟', 'FEMALE', '讲师', 7, '13800001045', 'shengjuan@university.edu', NOW(), NOW()),
(47, 'T046', '顾浩', 'MALE', '教授', 7, '13800001046', 'guhao@university.edu', NOW(), NOW()),
(48, 'T047', '毛丽', 'FEMALE', '副教授', 8, '13800001047', 'maoli@university.edu', NOW(), NOW()),
(49, 'T048', '邱伟', 'MALE', '讲师', 8, '13800001048', 'qiuwei@university.edu', NOW(), NOW()),
(50, 'T049', '秦娟', 'FEMALE', '教授', 1, '13800001049', 'qinjuan@university.edu', NOW(), NOW()),
(51, 'T050', '江鹏', 'MALE', '副教授', 1, '13800001050', 'jiangpeng@university.edu', NOW(), NOW());

-- 4. 插入学生账户和学生数据 (200名学生)
-- 先插入学生账户
INSERT INTO sys_user (username, password, role, enabled, first_login, created_at, updated_at)
SELECT 
    CONCAT('S', LPAD(num, 6, '0')),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lccxKg3NXYPpNIcPi',
    'STUDENT',
    TRUE,
    FALSE,
    NOW(),
    NOW()
FROM (
    SELECT @row := @row + 1 as num
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t3,
         (SELECT @row := 0) r
    LIMIT 200
) numbers;

-- 插入学生详细信息
INSERT INTO student (user_id, student_no, name, gender, birth_date, enrollment_year, major_id, class_name, phone, deleted, created_at, updated_at)
SELECT 
    u.id,
    u.username,
    CONCAT(
        CASE FLOOR(RAND() * 100) % 10
            WHEN 0 THEN '李'
            WHEN 1 THEN '王'
            WHEN 2 THEN '张'
            WHEN 3 THEN '刘'
            WHEN 4 THEN '陈'
            WHEN 5 THEN '杨'
            WHEN 6 THEN '赵'
            WHEN 7 THEN '黄'
            WHEN 8 THEN '周'
            ELSE '吴'
        END,
        CASE FLOOR(RAND() * 100) % 20
            WHEN 0 THEN '明' WHEN 1 THEN '华' WHEN 2 THEN '建' WHEN 3 THEN '文' WHEN 4 THEN '军'
            WHEN 5 THEN '杰' WHEN 6 THEN '峰' WHEN 7 THEN '磊' WHEN 8 THEN '强' WHEN 9 THEN '伟'
            WHEN 10 THEN '丽' WHEN 11 THEN '娜' WHEN 12 THEN '芳' WHEN 13 THEN '敏' WHEN 14 THEN '静'
            WHEN 15 THEN '红' WHEN 16 THEN '燕' WHEN 17 THEN '霞' WHEN 18 THEN '艳' ELSE '秀'
        END,
        CASE FLOOR(RAND() * 100) % 20
            WHEN 0 THEN '华' WHEN 1 THEN '东' WHEN 2 THEN '明' WHEN 3 THEN '强' WHEN 4 THEN '军'
            WHEN 5 THEN '杰' WHEN 6 THEN '波' WHEN 7 THEN '涛' WHEN 8 THEN '鹏' WHEN 9 THEN '勇'
            WHEN 10 THEN '丽' WHEN 11 THEN '娜' WHEN 12 THEN '芳' WHEN 13 THEN '敏' WHEN 14 THEN '静'
            WHEN 15 THEN '红' WHEN 16 THEN '燕' WHEN 17 THEN '霞' WHEN 18 THEN '艳' ELSE '英'
        END
    ),
    IF(RAND() > 0.5, 'MALE', 'FEMALE'),
    DATE_SUB(CURDATE(), INTERVAL FLOOR(18 + RAND() * 6) YEAR),
    2021 + FLOOR(RAND() * 4),
    1 + FLOOR(RAND() * 21),
    CONCAT(
        CASE FLOOR(RAND() * 5)
            WHEN 0 THEN '1'
            WHEN 1 THEN '2'
            WHEN 2 THEN '3'
            WHEN 3 THEN '4'
            ELSE '5'
        END,
        '班'
    ),
    CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
    FALSE,
    NOW(),
    NOW()
FROM sys_user u
WHERE u.role = 'STUDENT'
ORDER BY u.id;

-- 5. 插入课程数据 (80门课程)
INSERT INTO course (course_no, name, credits, hours, type, department_id, description, created_at, updated_at) VALUES
-- 计算机科学学院课程
('CS101', '程序设计基础', 4, 64, 'REQUIRED', 1, '学习编程的基础知识，包括变量、控制结构、函数等', NOW(), NOW()),
('CS102', '数据结构', 4, 64, 'REQUIRED', 1, '学习线性表、树、图等基本数据结构及其算法', NOW(), NOW()),
('CS103', '算法设计与分析', 3, 48, 'REQUIRED', 1, '学习常见算法设计方法和复杂度分析', NOW(), NOW()),
('CS104', '操作系统', 4, 64, 'REQUIRED', 1, '学习操作系统原理、进程管理、内存管理等', NOW(), NOW()),
('CS105', '计算机网络', 3, 48, 'REQUIRED', 1, '学习网络协议、TCP/IP、网络安全等知识', NOW(), NOW()),
('CS106', '数据库系统', 4, 64, 'REQUIRED', 1, '学习数据库设计、SQL语言、事务处理等', NOW(), NOW()),
('CS107', '软件工程', 3, 48, 'REQUIRED', 1, '学习软件开发流程、需求分析、设计模式等', NOW(), NOW()),
('CS108', '编译原理', 3, 48, 'ELECTIVE', 1, '学习编译器的设计和实现', NOW(), NOW()),
('CS109', '人工智能导论', 3, 48, 'ELECTIVE', 1, '学习人工智能基础知识和应用', NOW(), NOW()),
('CS110', '机器学习', 3, 48, 'ELECTIVE', 1, '学习机器学习算法和应用', NOW(), NOW()),
('CS111', 'Web开发技术', 3, 48, 'ELECTIVE', 1, '学习前端和后端Web开发技术', NOW(), NOW()),
('CS112', '移动应用开发', 3, 48, 'ELECTIVE', 1, '学习Android或iOS应用开发', NOW(), NOW()),
('CS113', '云计算技术', 3, 48, 'ELECTIVE', 1, '学习云计算架构和服务', NOW(), NOW()),
('CS114', '大数据技术', 3, 48, 'ELECTIVE', 1, '学习大数据处理框架和技术', NOW(), NOW()),
('CS115', '计算机图形学', 3, 48, 'ELECTIVE', 1, '学习2D/3D图形渲染技术', NOW(), NOW()),

-- 数学学院课程
('MATH101', '高等数学I', 5, 80, 'REQUIRED', 2, '学习微积分基础知识', NOW(), NOW()),
('MATH102', '高等数学II', 5, 80, 'REQUIRED', 2, '学习多元函数微积分和级数', NOW(), NOW()),
('MATH103', '线性代数', 4, 64, 'REQUIRED', 2, '学习矩阵理论和线性方程组', NOW(), NOW()),
('MATH104', '概率论与数理统计', 4, 64, 'REQUIRED', 2, '学习概率论基础和统计推断', NOW(), NOW()),
('MATH105', '离散数学', 3, 48, 'REQUIRED', 2, '学习集合论、图论、组合数学等', NOW(), NOW()),
('MATH106', '数学分析', 4, 64, 'ELECTIVE', 2, '深入学习实数理论和函数极限', NOW(), NOW()),
('MATH107', '复变函数', 3, 48, 'ELECTIVE', 2, '学习复数和复变函数理论', NOW(), NOW()),
('MATH108', '数值分析', 3, 48, 'ELECTIVE', 2, '学习数值计算方法', NOW(), NOW()),
('MATH109', '运筹学', 3, 48, 'ELECTIVE', 2, '学习优化理论和方法', NOW(), NOW()),
('MATH110', '随机过程', 3, 48, 'ELECTIVE', 2, '学习随机过程的理论和应用', NOW(), NOW()),

-- 物理学院课程
('PHYS101', '大学物理I', 4, 64, 'REQUIRED', 3, '学习力学和热学', NOW(), NOW()),
('PHYS102', '大学物理II', 4, 64, 'REQUIRED', 3, '学习电磁学和光学', NOW(), NOW()),
('PHYS103', '量子力学', 4, 64, 'REQUIRED', 3, '学习量子力学基本原理', NOW(), NOW()),
('PHYS104', '热力学与统计物理', 3, 48, 'REQUIRED', 3, '学习热力学和统计物理学', NOW(), NOW()),
('PHYS105', '固体物理', 3, 48, 'ELECTIVE', 3, '学习固体的结构和性质', NOW(), NOW()),
('PHYS106', '原子物理学', 3, 48, 'ELECTIVE', 3, '学习原子结构和光谱', NOW(), NOW()),
('PHYS107', '理论力学', 4, 64, 'ELECTIVE', 3, '学习经典力学理论', NOW(), NOW()),
('PHYS108', '电动力学', 3, 48, 'ELECTIVE', 3, '学习电磁场理论', NOW(), NOW()),

-- 化学学院课程
('CHEM101', '无机化学', 4, 64, 'REQUIRED', 4, '学习无机化学基础', NOW(), NOW()),
('CHEM102', '有机化学', 4, 64, 'REQUIRED', 4, '学习有机化合物的结构和性质', NOW(), NOW()),
('CHEM103', '物理化学', 4, 64, 'REQUIRED', 4, '学习化学热力学和动力学', NOW(), NOW()),
('CHEM104', '分析化学', 3, 48, 'REQUIRED', 4, '学习化学分析方法', NOW(), NOW()),
('CHEM105', '仪器分析', 3, 48, 'ELECTIVE', 4, '学习现代仪器分析技术', NOW(), NOW()),
('CHEM106', '高分子化学', 3, 48, 'ELECTIVE', 4, '学习高分子材料', NOW(), NOW()),
('CHEM107', '生物化学', 3, 48, 'ELECTIVE', 4, '学习生物分子的化学', NOW(), NOW()),
('CHEM108', '化学工程', 3, 48, 'ELECTIVE', 4, '学习化学工艺和设备', NOW(), NOW()),

-- 生物学院课程
('BIO101', '普通生物学', 3, 48, 'REQUIRED', 5, '学习生物学基础知识', NOW(), NOW()),
('BIO102', '细胞生物学', 4, 64, 'REQUIRED', 5, '学习细胞的结构和功能', NOW(), NOW()),
('BIO103', '遗传学', 4, 64, 'REQUIRED', 5, '学习遗传规律和基因理论', NOW(), NOW()),
('BIO104', '分子生物学', 4, 64, 'REQUIRED', 5, '学习DNA、RNA、蛋白质的分子机制', NOW(), NOW()),
('BIO105', '生物化学', 4, 64, 'REQUIRED', 5, '学习生物大分子的化学', NOW(), NOW()),
('BIO106', '微生物学', 3, 48, 'ELECTIVE', 5, '学习微生物的特性和应用', NOW(), NOW()),
('BIO107', '免疫学', 3, 48, 'ELECTIVE', 5, '学习免疫系统和免疫反应', NOW(), NOW()),
('BIO108', '生物信息学', 3, 48, 'ELECTIVE', 5, '学习生物数据分析方法', NOW(), NOW()),

-- 工程学院课程
('ENG101', '工程制图', 3, 48, 'REQUIRED', 6, '学习机械制图和CAD', NOW(), NOW()),
('ENG102', '工程力学', 4, 64, 'REQUIRED', 6, '学习静力学和材料力学', NOW(), NOW()),
('ENG103', '电路分析', 4, 64, 'REQUIRED', 6, '学习电路理论和分析方法', NOW(), NOW()),
('ENG104', '模拟电子技术', 3, 48, 'REQUIRED', 6, '学习模拟电路设计', NOW(), NOW()),
('ENG105', '数字电子技术', 3, 48, 'REQUIRED', 6, '学习数字电路和逻辑设计', NOW(), NOW()),
('ENG106', '控制理论', 3, 48, 'ELECTIVE', 6, '学习自动控制系统', NOW(), NOW()),
('ENG107', '机械设计', 3, 48, 'ELECTIVE', 6, '学习机械零件设计', NOW(), NOW()),
('ENG108', '嵌入式系统', 3, 48, 'ELECTIVE', 6, '学习嵌入式开发', NOW(), NOW()),

-- 经济学院课程
('ECON101', '微观经济学', 3, 48, 'REQUIRED', 7, '学习个体经济行为', NOW(), NOW()),
('ECON102', '宏观经济学', 3, 48, 'REQUIRED', 7, '学习国民经济运行', NOW(), NOW()),
('ECON103', '计量经济学', 3, 48, 'REQUIRED', 7, '学习经济数据分析', NOW(), NOW()),
('ECON104', '金融学', 3, 48, 'ELECTIVE', 7, '学习金融市场和工具', NOW(), NOW()),
('ECON105', '国际贸易', 3, 48, 'ELECTIVE', 7, '学习国际贸易理论', NOW(), NOW()),
('ECON106', '货币银行学', 3, 48, 'ELECTIVE', 7, '学习货币和银行体系', NOW(), NOW()),
('ECON107', '投资学', 3, 48, 'ELECTIVE', 7, '学习投资理论和实践', NOW(), NOW()),

-- 管理学院课程
('MGMT101', '管理学原理', 3, 48, 'REQUIRED', 8, '学习管理的基本理论', NOW(), NOW()),
('MGMT102', '会计学基础', 3, 48, 'REQUIRED', 8, '学习会计基本原理', NOW(), NOW()),
('MGMT103', '财务管理', 3, 48, 'REQUIRED', 8, '学习企业财务决策', NOW(), NOW()),
('MGMT104', '市场营销', 3, 48, 'ELECTIVE', 8, '学习市场营销策略', NOW(), NOW()),
('MGMT105', '人力资源管理', 3, 48, 'ELECTIVE', 8, '学习人力资源管理', NOW(), NOW()),
('MGMT106', '战略管理', 3, 48, 'ELECTIVE', 8, '学习企业战略', NOW(), NOW()),
('MGMT107', '项目管理', 3, 48, 'ELECTIVE', 8, '学习项目管理方法', NOW(), NOW()),

-- 公共课程
('PUB101', '大学英语I', 4, 64, 'PUBLIC', 1, '提高英语综合能力', NOW(), NOW()),
('PUB102', '大学英语II', 4, 64, 'PUBLIC', 1, '进一步提高英语能力', NOW(), NOW()),
('PUB103', '体育I', 1, 32, 'PUBLIC', 1, '体育锻炼和健康', NOW(), NOW()),
('PUB104', '体育II', 1, 32, 'PUBLIC', 1, '体育锻炼和健康', NOW(), NOW()),
('PUB105', '思想道德与法治', 3, 48, 'PUBLIC', 1, '思想政治教育', NOW(), NOW()),
('PUB106', '中国近现代史纲要', 3, 48, 'PUBLIC', 1, '历史教育', NOW(), NOW()),
('PUB107', '马克思主义基本原理', 3, 48, 'PUBLIC', 1, '政治理论课', NOW(), NOW());

-- 6. 插入学期数据
INSERT INTO semester (academic_year, semester_type, start_date, end_date, course_selection_start, course_selection_end, active, created_at, updated_at) VALUES
('2023-2024', 1, '2023-09-01', '2024-01-15', '2023-08-15 09:00:00', '2023-09-10 18:00:00', FALSE, NOW(), NOW()),
('2023-2024', 2, '2024-02-20', '2024-06-30', '2024-02-05 09:00:00', '2024-03-05 18:00:00', FALSE, NOW(), NOW()),
('2024-2025', 1, '2024-09-01', '2025-01-15', '2024-08-15 09:00:00', '2024-09-10 18:00:00', TRUE, NOW(), NOW()),
('2024-2025', 2, '2025-02-20', '2025-06-30', '2025-02-05 09:00:00', '2025-03-05 18:00:00', FALSE, NOW(), NOW());

-- 7. 插入开课计划数据 (当前学期: 2024-2025秋季)
INSERT INTO course_offering (semester_id, course_id, teacher_id, schedule, location, capacity, enrolled, status, created_at, updated_at) VALUES
-- 计算机科学课程
(3, 1, 1, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A101', 120, 0, 'PUBLISHED', NOW(), NOW()),
(3, 2, 2, '[{"day":2,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A102', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 3, 3, '[{"day":3,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A103', 80, 0, 'PUBLISHED', NOW(), NOW()),
(3, 4, 4, '[{"day":4,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A104', 90, 0, 'PUBLISHED', NOW(), NOW()),
(3, 5, 5, '[{"day":5,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A105', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 6, 6, '[{"day":1,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A106', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 7, 1, '[{"day":2,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A107', 80, 0, 'PUBLISHED', NOW(), NOW()),
(3, 8, 2, '[{"day":3,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A108', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 9, 3, '[{"day":4,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A109', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 10, 4, '[{"day":5,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼A110', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 11, 5, '[{"day":1,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '实验楼B201', 50, 0, 'PUBLISHED', NOW(), NOW()),
(3, 12, 6, '[{"day":2,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '实验楼B202', 50, 0, 'PUBLISHED', NOW(), NOW()),
(3, 13, 1, '[{"day":3,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '实验楼B203', 50, 0, 'PUBLISHED', NOW(), NOW()),
(3, 14, 2, '[{"day":4,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '实验楼B204', 50, 0, 'PUBLISHED', NOW(), NOW()),
(3, 15, 3, '[{"day":5,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '实验楼B205', 40, 0, 'PUBLISHED', NOW(), NOW()),

-- 数学课程
(3, 16, 7, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C101', 150, 0, 'PUBLISHED', NOW(), NOW()),
(3, 17, 8, '[{"day":2,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C102', 150, 0, 'PUBLISHED', NOW(), NOW()),
(3, 18, 9, '[{"day":3,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C103', 120, 0, 'PUBLISHED', NOW(), NOW()),
(3, 19, 10, '[{"day":4,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C104', 120, 0, 'PUBLISHED', NOW(), NOW()),
(3, 20, 7, '[{"day":5,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C105', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 21, 8, '[{"day":1,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C106', 80, 0, 'PUBLISHED', NOW(), NOW()),
(3, 22, 9, '[{"day":2,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C107', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 23, 10, '[{"day":3,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C108', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 24, 7, '[{"day":4,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C109', 60, 0, 'PUBLISHED', NOW(), NOW()),
(3, 25, 8, '[{"day":5,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼C110', 60, 0, 'PUBLISHED', NOW(), NOW()),

-- 物理课程
(3, 26, 11, '[{"day":1,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼D101', 120, 0, 'PUBLISHED', NOW(), NOW()),
(3, 27, 12, '[{"day":2,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼D102', 120, 0, 'PUBLISHED', NOW(), NOW()),
(3, 28, 13, '[{"day":3,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼D103', 80, 0, 'PUBLISHED', NOW(), NOW()),
(3, 29, 11, '[{"day":4,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼D104', 80, 0, 'PUBLISHED', NOW(), NOW()),
(3, 30, 12, '[{"day":5,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼D105', 60, 0, 'PUBLISHED', NOW(), NOW()),

-- 公共课程
(3, 74, 25, '[{"day":1,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E101', 200, 0, 'PUBLISHED', NOW(), NOW()),
(3, 74, 25, '[{"day":1,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E102', 200, 0, 'PUBLISHED', NOW(), NOW()),
(3, 75, 26, '[{"day":2,"period":"1-2","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E103', 200, 0, 'PUBLISHED', NOW(), NOW()),
(3, 75, 26, '[{"day":2,"period":"3-4","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E104', 200, 0, 'PUBLISHED', NOW(), NOW()),
(3, 76, 27, '[{"day":3,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 76, 28, '[{"day":3,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 77, 27, '[{"day":4,"period":"5-6","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 77, 28, '[{"day":4,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '体育馆', 100, 0, 'PUBLISHED', NOW(), NOW()),
(3, 78, 29, '[{"day":1,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E201', 150, 0, 'PUBLISHED', NOW(), NOW()),
(3, 79, 30, '[{"day":2,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E202', 150, 0, 'PUBLISHED', NOW(), NOW()),
(3, 80, 31, '[{"day":3,"period":"7-8","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]}]', '教学楼E203', 150, 0, 'PUBLISHED', NOW(), NOW());

-- 提交
COMMIT;

-- 验证数据
SELECT '院系数量：', COUNT(*) FROM department;
SELECT '专业数量：', COUNT(*) FROM major;
SELECT '教师数量：', COUNT(*) FROM teacher;
SELECT '学生数量：', COUNT(*) FROM student;
SELECT '课程数量：', COUNT(*) FROM course;
SELECT '学期数量：', COUNT(*) FROM semester;
SELECT '开课计划数量：', COUNT(*) FROM course_offering;

SELECT '✓ 测试数据插入完成！' as status;
SELECT '管理员账户: admin / admin123' as info;
SELECT '教师账户示例: T001 / teacher123' as info;
SELECT '学生账户示例: S000001 / student123' as info;

