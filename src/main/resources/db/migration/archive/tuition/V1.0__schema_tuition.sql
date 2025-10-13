-- ============================================
-- 学费缴纳模块数据库表结构
-- Version: 1.0
-- Description: 学费标准、账单、缴费记录、退费管理
-- ============================================

-- --------------------------------------------
-- 1. 学费标准表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS tuition_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年（如：2024-2025）',
    grade_level INT NOT NULL COMMENT '年级（1-4）',
    tuition_fee DECIMAL(10, 2) NOT NULL COMMENT '学费',
    accommodation_fee DECIMAL(10, 2) NOT NULL COMMENT '住宿费',
    textbook_fee DECIMAL(10, 2) NOT NULL COMMENT '教材费',
    other_fees DECIMAL(10, 2) DEFAULT 0.00 COMMENT '其他费用',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_tuition_standard_major FOREIGN KEY (major_id) REFERENCES major(id),
    CONSTRAINT uk_tuition_standard_unique UNIQUE (major_id, academic_year, grade_level, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学费标准表';

-- --------------------------------------------
-- 2. 学费账单表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS tuition_bill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    tuition_fee DECIMAL(10, 2) NOT NULL COMMENT '学费',
    accommodation_fee DECIMAL(10, 2) NOT NULL COMMENT '住宿费',
    textbook_fee DECIMAL(10, 2) NOT NULL COMMENT '教材费',
    other_fees DECIMAL(10, 2) DEFAULT 0.00 COMMENT '其他费用',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '应缴总额',
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额',
    outstanding_amount DECIMAL(10, 2) NOT NULL COMMENT '欠费金额',
    status VARCHAR(20) NOT NULL COMMENT '账单状态：UNPAID, PARTIAL, PAID, OVERDUE',
    due_date DATE NOT NULL COMMENT '缴费截止日期',
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_tuition_bill_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT uk_tuition_bill_unique UNIQUE (student_id, academic_year, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学费账单表';

-- --------------------------------------------
-- 3. 缴费记录表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS tuition_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    payment_no VARCHAR(50) NOT NULL COMMENT '缴费单号',
    amount DECIMAL(10, 2) NOT NULL COMMENT '缴费金额',
    method VARCHAR(20) NOT NULL COMMENT '缴费方式：ALIPAY, WECHAT, BANK_CARD, CASH, BANK_TRANSFER',
    status VARCHAR(20) NOT NULL COMMENT '缴费状态：PENDING, SUCCESS, FAILED, REFUNDED',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    paid_at DATETIME COMMENT '缴费时间',
    receipt_url VARCHAR(500) COMMENT '电子收据URL',
    operator_id BIGINT COMMENT '线下缴费操作员ID',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_tuition_payment_bill FOREIGN KEY (bill_id) REFERENCES tuition_bill(id),
    CONSTRAINT fk_tuition_payment_operator FOREIGN KEY (operator_id) REFERENCES user(id),
    CONSTRAINT uk_payment_no UNIQUE (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费记录表';

-- --------------------------------------------
-- 4. 缴费提醒表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS payment_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    reminder_type VARCHAR(20) NOT NULL COMMENT '提醒类型：EMAIL, SMS, NOTIFICATION',
    sent_at DATETIME NOT NULL COMMENT '发送时间',
    success BOOLEAN NOT NULL COMMENT '是否成功',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_payment_reminder_bill FOREIGN KEY (bill_id) REFERENCES tuition_bill(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费提醒表';

-- --------------------------------------------
-- 5. 退费申请表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS refund_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    payment_id BIGINT NOT NULL COMMENT '缴费记录ID',
    refund_amount DECIMAL(10, 2) NOT NULL COMMENT '退费金额',
    reason TEXT NOT NULL COMMENT '退费原因',
    refund_type VARCHAR(20) NOT NULL COMMENT '退费类型：FULL_REFUND, PARTIAL_REFUND',
    refund_method VARCHAR(20) NOT NULL COMMENT '退费方式：ORIGINAL_PATH, BANK_TRANSFER, CASH',
    bank_account VARCHAR(100) COMMENT '银行账号（银行转账时必填）',
    status VARCHAR(20) NOT NULL COMMENT '审批状态：PENDING, FINANCIAL_APPROVED, APPROVED, REJECTED, CANCELLED, REFUNDED',
    approval_level INT NOT NULL DEFAULT 1 COMMENT '当前审批级别：1-财务审核，2-管理员审批',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    submitted_at DATETIME NOT NULL COMMENT '提交时间',
    approved_at DATETIME COMMENT '审批完成时间',
    refunded_at DATETIME COMMENT '退费完成时间',
    refund_transaction_id VARCHAR(100) COMMENT '退费交易流水号',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_refund_application_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_refund_application_payment FOREIGN KEY (payment_id) REFERENCES tuition_payment(id),
    CONSTRAINT fk_refund_application_approver FOREIGN KEY (current_approver_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退费申请表';

-- --------------------------------------------
-- 6. 退费审批记录表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS refund_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    refund_application_id BIGINT NOT NULL COMMENT '退费申请ID',
    approval_level INT NOT NULL COMMENT '审批级别：1-财务审核，2-管理员审批',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    action VARCHAR(20) NOT NULL COMMENT '审批操作：APPROVE, REJECT, RETURN',
    comment TEXT COMMENT '审批意见',
    approved_at DATETIME NOT NULL COMMENT '审批时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_refund_approval_application FOREIGN KEY (refund_application_id) REFERENCES refund_application(id),
    CONSTRAINT fk_refund_approval_approver FOREIGN KEY (approver_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退费审批记录表';

-- ============================================
-- 索引创建
-- ============================================

-- 学费标准表索引
CREATE INDEX idx_tuition_standard_major ON tuition_standard(major_id);
CREATE INDEX idx_tuition_standard_year ON tuition_standard(academic_year);
CREATE INDEX idx_tuition_standard_active ON tuition_standard(active);

-- 学费账单表索引
CREATE INDEX idx_tuition_bill_student ON tuition_bill(student_id);
CREATE INDEX idx_tuition_bill_year ON tuition_bill(academic_year);
CREATE INDEX idx_tuition_bill_status ON tuition_bill(status);
CREATE INDEX idx_tuition_bill_due_date ON tuition_bill(due_date);

-- 缴费记录表索引
CREATE INDEX idx_tuition_payment_bill ON tuition_payment(bill_id);
CREATE INDEX idx_tuition_payment_no ON tuition_payment(payment_no);
CREATE INDEX idx_tuition_payment_status ON tuition_payment(status);
CREATE INDEX idx_tuition_payment_method ON tuition_payment(method);
CREATE INDEX idx_tuition_payment_paid_at ON tuition_payment(paid_at);

-- 缴费提醒表索引
CREATE INDEX idx_payment_reminder_bill ON payment_reminder(bill_id);
CREATE INDEX idx_payment_reminder_sent_at ON payment_reminder(sent_at);

-- 退费申请表索引
CREATE INDEX idx_refund_application_student ON refund_application(student_id);
CREATE INDEX idx_refund_application_payment ON refund_application(payment_id);
CREATE INDEX idx_refund_application_status ON refund_application(status);
CREATE INDEX idx_refund_application_level ON refund_application(approval_level);
CREATE INDEX idx_refund_application_approver ON refund_application(current_approver_id);

-- 退费审批记录表索引
CREATE INDEX idx_refund_approval_application ON refund_approval(refund_application_id);
CREATE INDEX idx_refund_approval_level ON refund_approval(approval_level);
CREATE INDEX idx_refund_approval_approver ON refund_approval(approver_id);

-- ============================================
-- 视图创建（可选）
-- ============================================

-- 学生账单详情视图
CREATE OR REPLACE VIEW v_student_bill_detail AS
SELECT 
    tb.id AS bill_id,
    tb.academic_year,
    tb.total_amount,
    tb.paid_amount,
    tb.outstanding_amount,
    tb.status,
    tb.due_date,
    tb.generated_at,
    s.id AS student_id,
    s.student_no,
    s.name AS student_name,
    s.enrollment_year,
    m.id AS major_id,
    m.name AS major_name,
    d.id AS department_id,
    d.name AS department_name
FROM tuition_bill tb
JOIN student s ON tb.student_id = s.id
JOIN major m ON s.major_id = m.id
JOIN department d ON m.department_id = d.id
WHERE tb.deleted = FALSE AND s.deleted = FALSE;

-- 缴费记录详情视图
CREATE OR REPLACE VIEW v_payment_detail AS
SELECT 
    tp.id AS payment_id,
    tp.payment_no,
    tp.amount,
    tp.method,
    tp.status,
    tp.transaction_id,
    tp.paid_at,
    tb.id AS bill_id,
    tb.academic_year,
    s.id AS student_id,
    s.student_no,
    s.name AS student_name,
    m.name AS major_name,
    d.name AS department_name,
    u.username AS operator_name
FROM tuition_payment tp
JOIN tuition_bill tb ON tp.bill_id = tb.id
JOIN student s ON tb.student_id = s.id
JOIN major m ON s.major_id = m.id
JOIN department d ON m.department_id = d.id
LEFT JOIN user u ON tp.operator_id = u.id
WHERE tp.deleted = FALSE AND tb.deleted = FALSE AND s.deleted = FALSE;

-- ============================================
-- 完成
-- ============================================

