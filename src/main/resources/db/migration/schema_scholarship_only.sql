-- =============================================
-- 奖学金评定模块数据库Schema（仅表结构）
-- =============================================

-- 1. 奖学金类型表
CREATE TABLE IF NOT EXISTS scholarship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '奖学金名称',
    level VARCHAR(20) NOT NULL COMMENT '等级：NATIONAL-国家级, PROVINCIAL-省级, UNIVERSITY-校级, DEPARTMENT-院系级',
    amount DOUBLE NOT NULL COMMENT '奖金金额',
    quota INT NOT NULL COMMENT '名额',
    min_gpa DOUBLE COMMENT '最低GPA要求',
    min_credits DOUBLE COMMENT '最低学分要求',
    description TEXT COMMENT '奖学金描述',
    requirements TEXT COMMENT '其他要求（JSON格式）',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_scholarship_level (level),
    INDEX idx_scholarship_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金类型表';

-- 2. 奖学金申请表
CREATE TABLE IF NOT EXISTS scholarship_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    scholarship_id BIGINT NOT NULL COMMENT '奖学金ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    gpa DOUBLE NOT NULL COMMENT '学年GPA',
    total_credits DOUBLE NOT NULL COMMENT '总学分',
    comprehensive_score DOUBLE COMMENT '综合得分',
    personal_statement TEXT NOT NULL COMMENT '个人陈述',
    attachment_url VARCHAR(500) COMMENT '证明材料URL',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核, COUNSELOR_APPROVED-辅导员已通过, DEPT_APPROVED-已通过, REJECTED-已拒绝',
    submitted_at DATETIME NOT NULL COMMENT '提交时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (scholarship_id) REFERENCES scholarship(id) ON DELETE RESTRICT,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    INDEX idx_application_student (student_id),
    INDEX idx_application_scholarship (scholarship_id),
    INDEX idx_application_year (academic_year),
    INDEX idx_application_status (status),
    INDEX idx_application_score (comprehensive_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金申请表';

-- 3. 奖学金审批记录表
CREATE TABLE IF NOT EXISTS scholarship_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    approval_level INT NOT NULL COMMENT '审批级别：1-辅导员, 2-院系',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：APPROVE-通过, REJECT-拒绝, RETURN-退回',
    comment TEXT COMMENT '审批意见',
    approved_at DATETIME NOT NULL COMMENT '审批时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (application_id) REFERENCES scholarship_application(id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    INDEX idx_approval_application (application_id),
    INDEX idx_approval_level (approval_level),
    INDEX idx_approval_approver (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金审批记录表';

-- 4. 奖学金获奖记录表
CREATE TABLE IF NOT EXISTS scholarship_award (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    scholarship_id BIGINT NOT NULL COMMENT '奖学金ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    amount DOUBLE NOT NULL COMMENT '奖金金额',
    awarded_at DATETIME NOT NULL COMMENT '获奖时间',
    published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已公示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (application_id) REFERENCES scholarship_application(id) ON DELETE RESTRICT,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (scholarship_id) REFERENCES scholarship(id) ON DELETE RESTRICT,
    INDEX idx_award_student (student_id),
    INDEX idx_award_scholarship (scholarship_id),
    INDEX idx_award_year (academic_year),
    INDEX idx_award_published (published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金获奖记录表';

-- =============================================
-- 创建复合索引以优化常见查询
-- =============================================

-- 优化按学年和奖学金查询申请
CREATE INDEX IF NOT EXISTS idx_application_scholarship_year ON scholarship_application(scholarship_id, academic_year);

-- 优化按学生和学年查询申请
CREATE INDEX IF NOT EXISTS idx_application_student_year ON scholarship_application(student_id, academic_year);

-- 优化按学生和学年查询获奖记录
CREATE INDEX IF NOT EXISTS idx_award_student_year ON scholarship_award(student_id, academic_year);

-- 优化按奖学金和学年查询获奖记录
CREATE INDEX IF NOT EXISTS idx_award_scholarship_year ON scholarship_award(scholarship_id, academic_year);

