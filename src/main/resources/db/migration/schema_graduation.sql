-- ==================== 毕业审核模块数据库表结构 ====================
-- 创建日期: 2025-10-11
-- 描述: 毕业审核、毕业要求、学分汇总相关表

-- 1. 毕业要求表
CREATE TABLE IF NOT EXISTS graduation_requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    enrollment_year INT NOT NULL COMMENT '入学年份',
    total_credits_required DECIMAL(5,1) NOT NULL COMMENT '总学分要求',
    required_credits_required DECIMAL(5,1) NOT NULL COMMENT '必修学分要求',
    elective_credits_required DECIMAL(5,1) NOT NULL COMMENT '选修学分要求',
    practical_credits_required DECIMAL(5,1) NOT NULL COMMENT '实践学分要求',
    additional_requirements TEXT COMMENT '附加要求(JSON格式)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_major_year UNIQUE (major_id, enrollment_year),
    CONSTRAINT fk_gr_major FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE RESTRICT,
    INDEX idx_gr_major (major_id),
    INDEX idx_gr_year (enrollment_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业要求表';

-- 2. 学生学分汇总表
CREATE TABLE IF NOT EXISTS student_credit_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    total_credits DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '总学分',
    required_credits DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '必修学分',
    elective_credits DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '选修学分',
    practical_credits DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '实践学分',
    gpa DECIMAL(3,2) COMMENT '平均绩点(GPA)',
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_student UNIQUE (student_id),
    CONSTRAINT fk_scs_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    INDEX idx_scs_total_credits (total_credits),
    INDEX idx_scs_gpa (gpa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学分汇总表';

-- 3. 毕业审核表
CREATE TABLE IF NOT EXISTS graduation_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    audit_year INT NOT NULL COMMENT '审核年份',
    total_credits DECIMAL(5,1) NOT NULL COMMENT '总学分',
    required_credits DECIMAL(5,1) NOT NULL COMMENT '必修学分',
    elective_credits DECIMAL(5,1) NOT NULL COMMENT '选修学分',
    practical_credits DECIMAL(5,1) NOT NULL COMMENT '实践学分',
    status VARCHAR(20) NOT NULL COMMENT '审核状态: PENDING-待审核, PASS-通过, FAIL-不通过, DEFERRED-暂缓',
    fail_reason TEXT COMMENT '不通过原因',
    audited_by BIGINT COMMENT '审核人ID',
    audited_at DATETIME COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_ga_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_ga_auditor FOREIGN KEY (audited_by) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_ga_student (student_id),
    INDEX idx_ga_year (audit_year),
    INDEX idx_ga_status (status),
    INDEX idx_ga_audited_at (audited_at),
    INDEX idx_ga_student_year (student_id, audit_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业审核表';

-- 添加注释
ALTER TABLE graduation_requirement COMMENT = '毕业要求表 - 存储各专业各年级的毕业学分要求';
ALTER TABLE student_credit_summary COMMENT = '学生学分汇总表 - 缓存学生当前学分统计信息';
ALTER TABLE graduation_audit COMMENT = '毕业审核表 - 记录学生毕业审核结果';

