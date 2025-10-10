-- 考试表
CREATE TABLE exam (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '考试名称',
    type VARCHAR(20) NOT NULL COMMENT '考试类型：MIDTERM-期中考试，FINAL-期末考试，MAKEUP-补考，RETAKE-重修考试',
    course_offering_id BIGINT NOT NULL COMMENT '开课计划ID',
    exam_time DATETIME NOT NULL COMMENT '考试时间',
    duration INT NOT NULL COMMENT '考试时长（分钟）',
    total_score INT NOT NULL DEFAULT 100 COMMENT '总分',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '考试状态：DRAFT-草稿，PUBLISHED-已发布，IN_PROGRESS-进行中，FINISHED-已结束，CANCELLED-已取消',
    description TEXT COMMENT '考试说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_offering_id) REFERENCES course_offering(id),
    INDEX idx_exam_offering (course_offering_id),
    INDEX idx_exam_time (exam_time),
    INDEX idx_exam_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考场表
CREATE TABLE exam_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    room_name VARCHAR(100) NOT NULL COMMENT '考场名称',
    location VARCHAR(200) NOT NULL COMMENT '考场地点',
    capacity INT NOT NULL COMMENT '考场容量',
    assigned_count INT NOT NULL DEFAULT 0 COMMENT '已分配人数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE,
    INDEX idx_room_exam (exam_id),
    INDEX idx_room_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考场表';

-- 考场学生分配表
CREATE TABLE exam_room_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_room_id BIGINT NOT NULL COMMENT '考场ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    seat_number VARCHAR(20) NOT NULL COMMENT '座位号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_room_id) REFERENCES exam_room(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id),
    UNIQUE KEY uk_room_student (exam_room_id, student_id),
    INDEX idx_ers_room (exam_room_id),
    INDEX idx_ers_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考场学生分配表';

-- 监考安排表
CREATE TABLE exam_invigilator (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_room_id BIGINT NOT NULL COMMENT '考场ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    type VARCHAR(20) NOT NULL COMMENT '监考类型：CHIEF-主监考，ASSISTANT-副监考',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_room_id) REFERENCES exam_room(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    INDEX idx_invig_room (exam_room_id),
    INDEX idx_invig_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监考安排表';

