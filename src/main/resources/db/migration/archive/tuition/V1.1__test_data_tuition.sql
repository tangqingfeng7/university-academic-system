-- ============================================
-- 学费缴纳模块测试数据
-- Version: 1.1
-- Description: 学费标准、账单、缴费记录测试数据
-- ============================================

-- ============================================
-- 1. 学费标准测试数据
-- ============================================

-- 计算机科学与技术专业学费标准（2024-2025学年）
INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2024-2025', 
    1, 
    5000.00, 
    1200.00, 
    500.00, 
    0.00, 
    TRUE,
    NOW()
FROM major WHERE code = 'CS01'
LIMIT 1;

INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2024-2025', 
    2, 
    5000.00, 
    1200.00, 
    500.00, 
    0.00, 
    TRUE,
    NOW()
FROM major WHERE code = 'CS01'
LIMIT 1;

-- 软件工程专业学费标准（2024-2025学年）
INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2024-2025', 
    1, 
    5500.00, 
    1200.00, 
    600.00, 
    0.00, 
    TRUE,
    NOW()
FROM major WHERE code = 'CS02'
LIMIT 1;

-- 网络工程专业学费标准（2024-2025学年）
INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2024-2025', 
    1, 
    4800.00, 
    1200.00, 
    500.00, 
    0.00, 
    TRUE,
    NOW()
FROM major WHERE code = 'CS03'
LIMIT 1;

-- 人工智能专业学费标准（2024-2025学年）
INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2024-2025', 
    1, 
    4500.00, 
    1200.00, 
    400.00, 
    0.00, 
    TRUE,
    NOW()
FROM major WHERE code = 'CS04'
LIMIT 1;

-- 上一学年的学费标准（2023-2024学年）
INSERT INTO tuition_standard (major_id, academic_year, grade_level, tuition_fee, accommodation_fee, textbook_fee, other_fees, active, created_at)
SELECT 
    id, 
    '2023-2024', 
    2, 
    4800.00, 
    1000.00, 
    450.00, 
    0.00, 
    FALSE,
    NOW()
FROM major WHERE code = 'CS01'
LIMIT 1;

-- ============================================
-- 2. 学费账单测试数据
-- ============================================

-- 为计算机科学与技术专业一年级学生生成账单
INSERT INTO tuition_bill (student_id, academic_year, tuition_fee, accommodation_fee, textbook_fee, other_fees, total_amount, paid_amount, outstanding_amount, status, due_date, generated_at, created_at)
SELECT 
    s.id,
    '2024-2025',
    5000.00,
    1200.00,
    500.00,
    0.00,
    6700.00,
    0.00,
    6700.00,
    'UNPAID',
    '2024-09-30',
    '2024-08-01 10:00:00',
    NOW()
FROM student s
JOIN major m ON s.major_id = m.id
WHERE m.code = 'CS01' 
AND s.enrollment_year = 2024
LIMIT 5;

-- 为部分学生生成已缴费账单
INSERT INTO tuition_bill (student_id, academic_year, tuition_fee, accommodation_fee, textbook_fee, other_fees, total_amount, paid_amount, outstanding_amount, status, due_date, generated_at, created_at)
SELECT 
    s.id,
    '2024-2025',
    5000.00,
    1200.00,
    500.00,
    0.00,
    6700.00,
    6700.00,
    0.00,
    'PAID',
    '2024-09-30',
    '2024-08-01 10:00:00',
    NOW()
FROM student s
JOIN major m ON s.major_id = m.id
WHERE m.code = 'CS01' 
AND s.enrollment_year = 2024
LIMIT 1 OFFSET 5;

-- 为部分学生生成部分缴费账单
INSERT INTO tuition_bill (student_id, academic_year, tuition_fee, accommodation_fee, textbook_fee, other_fees, total_amount, paid_amount, outstanding_amount, status, due_date, generated_at, created_at)
SELECT 
    s.id,
    '2024-2025',
    5000.00,
    1200.00,
    500.00,
    0.00,
    6700.00,
    3000.00,
    3700.00,
    'PARTIAL',
    '2024-09-30',
    '2024-08-01 10:00:00',
    NOW()
FROM student s
JOIN major m ON s.major_id = m.id
WHERE m.code = 'CS01' 
AND s.enrollment_year = 2024
LIMIT 2 OFFSET 6;

-- 为软件工程专业学生生成账单
INSERT INTO tuition_bill (student_id, academic_year, tuition_fee, accommodation_fee, textbook_fee, other_fees, total_amount, paid_amount, outstanding_amount, status, due_date, generated_at, created_at)
SELECT 
    s.id,
    '2024-2025',
    5500.00,
    1200.00,
    600.00,
    0.00,
    7300.00,
    0.00,
    7300.00,
    'UNPAID',
    '2024-09-30',
    '2024-08-01 10:00:00',
    NOW()
FROM student s
JOIN major m ON s.major_id = m.id
WHERE m.code = 'CS02' 
AND s.enrollment_year = 2024
LIMIT 3;

-- ============================================
-- 3. 缴费记录测试数据
-- ============================================

-- 为已缴费的账单生成缴费记录（支付宝支付）
INSERT INTO tuition_payment (bill_id, payment_no, amount, method, status, transaction_id, paid_at, operator_id, created_at)
SELECT 
    tb.id,
    CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id, 8, '0')),
    6700.00,
    'ALIPAY',
    'SUCCESS',
    CONCAT('ALI', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), LPAD(tb.id, 10, '0')),
    '2024-08-15 14:30:00',
    NULL,
    NOW()
FROM tuition_bill tb
WHERE tb.status = 'PAID'
AND tb.academic_year = '2024-2025'
LIMIT 1;

-- 为部分缴费的账单生成缴费记录（微信支付）
INSERT INTO tuition_payment (bill_id, payment_no, amount, method, status, transaction_id, paid_at, operator_id, created_at)
SELECT 
    tb.id,
    CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id + 100, 8, '0')),
    3000.00,
    'WECHAT',
    'SUCCESS',
    CONCAT('WX', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), LPAD(tb.id, 10, '0')),
    '2024-08-20 09:15:00',
    NULL,
    NOW()
FROM tuition_bill tb
WHERE tb.status = 'PARTIAL'
AND tb.academic_year = '2024-2025'
LIMIT 1;

-- 线下银行转账缴费记录
INSERT INTO tuition_payment (bill_id, payment_no, amount, method, status, transaction_id, paid_at, operator_id, created_at)
SELECT 
    tb.id,
    CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id + 200, 8, '0')),
    6700.00,
    'BANK_TRANSFER',
    'SUCCESS',
    CONCAT('BANK', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id, 12, '0')),
    '2024-08-18 10:30:00',
    (SELECT id FROM sys_user WHERE role = 'ADMIN' LIMIT 1),
    NOW()
FROM tuition_bill tb
WHERE tb.status = 'PAID'
AND tb.academic_year = '2024-2025'
LIMIT 1 OFFSET 1;

-- 现金缴费记录
INSERT INTO tuition_payment (bill_id, payment_no, amount, method, status, transaction_id, paid_at, operator_id, created_at)
SELECT 
    tb.id,
    CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id + 300, 8, '0')),
    3000.00,
    'CASH',
    'SUCCESS',
    CONCAT('CASH', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(tb.id, 10, '0')),
    '2024-08-22 15:45:00',
    (SELECT id FROM sys_user WHERE role = 'ADMIN' LIMIT 1),
    NOW()
FROM tuition_bill tb
WHERE tb.status = 'PARTIAL'
AND tb.academic_year = '2024-2025'
LIMIT 1 OFFSET 1;

-- ============================================
-- 4. 缴费提醒测试数据
-- ============================================

-- 为未缴费账单生成提醒记录
INSERT INTO payment_reminder (bill_id, reminder_type, sent_at, success, created_at)
SELECT 
    tb.id,
    'NOTIFICATION',
    '2024-09-15 09:00:00',
    TRUE,
    NOW()
FROM tuition_bill tb
WHERE tb.status = 'UNPAID'
AND tb.academic_year = '2024-2025'
LIMIT 3;

-- 邮件提醒记录
INSERT INTO payment_reminder (bill_id, reminder_type, sent_at, success, created_at)
SELECT 
    tb.id,
    'EMAIL',
    '2024-09-20 10:00:00',
    TRUE,
    NOW()
FROM tuition_bill tb
WHERE tb.status IN ('UNPAID', 'PARTIAL')
AND tb.academic_year = '2024-2025'
LIMIT 5;

-- ============================================
-- 5. 退费申请测试数据（可选）
-- ============================================

-- 创建一个退费申请示例
INSERT INTO refund_application (
    student_id, 
    payment_id, 
    refund_amount, 
    reason, 
    refund_type, 
    refund_method, 
    bank_account,
    status, 
    approval_level,
    submitted_at,
    created_at
)
SELECT 
    tb.student_id,
    tp.id,
    1000.00,
    '因个人原因需要部分退费',
    'PARTIAL_REFUND',
    'BANK_TRANSFER',
    '6222021234567890123',
    'PENDING',
    1,
    '2024-09-01 14:00:00',
    NOW()
FROM tuition_payment tp
JOIN tuition_bill tb ON tp.bill_id = tb.id
WHERE tp.status = 'SUCCESS'
AND tp.method = 'ALIPAY'
LIMIT 1;

-- ============================================
-- 数据验证查询
-- ============================================

-- 查询学费标准数量
SELECT '学费标准总数：' AS description, COUNT(*) AS count FROM tuition_standard;

-- 查询账单数量
SELECT '学费账单总数：' AS description, COUNT(*) AS count FROM tuition_bill;

-- 查询缴费记录数量
SELECT '缴费记录总数：' AS description, COUNT(*) AS count FROM tuition_payment;

-- 查询账单状态分布
SELECT status AS '账单状态', COUNT(*) AS '数量' 
FROM tuition_bill 
WHERE academic_year = '2024-2025'
GROUP BY status;

-- 查询缴费方式分布
SELECT method AS '缴费方式', COUNT(*) AS '数量' 
FROM tuition_payment 
WHERE status = 'SUCCESS'
GROUP BY method;

-- ============================================
-- 完成
-- ============================================

