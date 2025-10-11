package com.university.academic.service.impl;

import com.university.academic.dto.*;
import com.university.academic.dto.converter.TuitionConverter;
import com.university.academic.entity.Major;
import com.university.academic.entity.Student;
import com.university.academic.entity.tuition.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import com.university.academic.service.TuitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 学费缴纳服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {

    private final TuitionStandardRepository standardRepository;
    private final TuitionBillRepository billRepository;
    private final TuitionPaymentRepository paymentRepository;
    private final PaymentReminderRepository reminderRepository;
    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;
    // private final NotificationService notificationService;
    private final TuitionConverter tuitionConverter;

    // ==================== 学费标准管理 ====================

    @Override
    @Transactional
    public TuitionStandardDTO setStandard(CreateTuitionStandardRequest request) {
        log.info("设置学费标准: majorId={}, academicYear={}, gradeLevel={}",
                request.getMajorId(), request.getAcademicYear(), request.getGradeLevel());

        // 验证专业是否存在
        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MAJOR_NOT_FOUND));

        // 检查是否已存在相同的学费标准
        if (standardRepository.existsByMajorAndYearAndGrade(
                request.getMajorId(),
                request.getAcademicYear(),
                request.getGradeLevel(),
                null)) {
            throw new BusinessException(ErrorCode.TUITION_STANDARD_ALREADY_EXISTS);
        }

        // 创建学费标准
        TuitionStandard standard = TuitionStandard.builder()
                .major(major)
                .academicYear(request.getAcademicYear())
                .gradeLevel(request.getGradeLevel())
                .tuitionFee(request.getTuitionFee())
                .accommodationFee(request.getAccommodationFee())
                .textbookFee(request.getTextbookFee())
                .otherFees(request.getOtherFees())
                .active(true)
                .build();

        standard = standardRepository.save(standard);
        log.info("学费标准创建成功: id={}", standard.getId());

        return tuitionConverter.toTuitionStandardDTO(standard);
    }

    @Override
    @Transactional
    public TuitionStandardDTO updateStandard(Long id, CreateTuitionStandardRequest request) {
        log.info("更新学费标准: id={}", id);

        TuitionStandard standard = findStandardById(id);

        // 检查是否已存在相同的学费标准（排除当前记录）
        if (standardRepository.existsByMajorAndYearAndGrade(
                request.getMajorId(),
                request.getAcademicYear(),
                request.getGradeLevel(),
                id)) {
            throw new BusinessException(ErrorCode.TUITION_STANDARD_ALREADY_EXISTS);
        }

        // 更新字段
        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MAJOR_NOT_FOUND));

        standard.setMajor(major);
        standard.setAcademicYear(request.getAcademicYear());
        standard.setGradeLevel(request.getGradeLevel());
        standard.setTuitionFee(request.getTuitionFee());
        standard.setAccommodationFee(request.getAccommodationFee());
        standard.setTextbookFee(request.getTextbookFee());
        standard.setOtherFees(request.getOtherFees());

        standard = standardRepository.save(standard);
        log.info("学费标准更新成功: id={}", id);

        return tuitionConverter.toTuitionStandardDTO(standard);
    }

    @Override
    public TuitionStandardDTO getStandard(Long id) {
        TuitionStandard standard = findStandardById(id);
        return tuitionConverter.toTuitionStandardDTO(standard);
    }

    @Override
    public List<TuitionStandardDTO> getStandardsByMajor(Long majorId) {
        List<TuitionStandard> standards = standardRepository.findByMajorId(majorId);
        return tuitionConverter.toTuitionStandardDTOList(standards);
    }

    @Override
    public List<TuitionStandardDTO> getStandardsByAcademicYear(String academicYear) {
        List<TuitionStandard> standards = standardRepository.findByAcademicYear(academicYear);
        return tuitionConverter.toTuitionStandardDTOList(standards);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TuitionStandardDTO> getStandards(String academicYear, Long majorId, 
                                                   Integer gradeLevel, Boolean active, Pageable pageable) {
        log.info("分页查询学费标准: academicYear={}, majorId={}, gradeLevel={}, active={}, page={}, size={}", 
                academicYear, majorId, gradeLevel, active, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<TuitionStandard> standardPage = standardRepository.searchStandards(
                academicYear, majorId, gradeLevel, active, pageable);
        
        return standardPage.map(tuitionConverter::toTuitionStandardDTO);
    }

    @Override
    @Transactional
    public void deactivateStandard(Long id) {
        log.info("停用学费标准: id={}", id);
        TuitionStandard standard = findStandardById(id);
        standard.setActive(false);
        standardRepository.save(standard);
    }

    // ==================== 学费账单管理 ====================

    @Override
    @Transactional
    public TuitionBillDTO generateBill(Long studentId, String academicYear) {
        log.info("生成学费账单: studentId={}, academicYear={}", studentId, academicYear);

        // 1. 检查是否已存在账单
        if (billRepository.existsByStudentIdAndAcademicYear(studentId, academicYear)) {
            throw new BusinessException(ErrorCode.TUITION_BILL_ALREADY_EXISTS);
        }

        // 2. 获取学生信息
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 获取学费标准
        Integer gradeLevel = calculateGradeLevel(student.getEnrollmentYear(), academicYear);
        TuitionStandard standard = standardRepository.findByMajorAndYearAndGrade(
                student.getMajor().getId(),
                academicYear,
                gradeLevel
        ).orElseThrow(() -> new BusinessException(ErrorCode.TUITION_STANDARD_NOT_FOUND));

        // 4. 创建账单
        LocalDate dueDate = calculateDueDate(academicYear);
        Double totalAmount = standard.getTotalFee();

        TuitionBill bill = TuitionBill.builder()
                .student(student)
                .academicYear(academicYear)
                .tuitionFee(standard.getTuitionFee())
                .accommodationFee(standard.getAccommodationFee())
                .textbookFee(standard.getTextbookFee())
                .otherFees(standard.getOtherFees())
                .totalAmount(totalAmount)
                .paidAmount(0.0)
                .outstandingAmount(totalAmount)
                .status(BillStatus.UNPAID)
                .dueDate(dueDate)
                .generatedAt(LocalDateTime.now())
                .build();

        bill = billRepository.save(bill);
        log.info("学费账单生成成功: billId={}, studentId={}, amount={}", 
                bill.getId(), studentId, totalAmount);

        // 5. 发送通知
        // TODO: 实现通知功能
        // try {
        //     notificationService.notifyBillGenerated(bill);
        // } catch (Exception e) {
        //     log.error("发送账单通知失败", e);
        // }

        return tuitionConverter.toTuitionBillDTO(bill);
    }

    @Override
    @Transactional
    public List<TuitionBillDTO> batchGenerateBills(String academicYear) {
        log.info("批量生成学费账单: academicYear={}", academicYear);

        List<Student> students = studentRepository.findAllActive();
        List<TuitionBill> bills = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        for (Student student : students) {
            try {
                // 检查是否已存在账单
                if (billRepository.existsByStudentIdAndAcademicYear(student.getId(), academicYear)) {
                    skipCount++;
                    continue;
                }

                // 获取学费标准
                Integer gradeLevel = calculateGradeLevel(student.getEnrollmentYear(), academicYear);
                TuitionStandard standard = standardRepository.findByMajorAndYearAndGrade(
                        student.getMajor().getId(),
                        academicYear,
                        gradeLevel
                ).orElse(null);

                if (standard == null) {
                    log.warn("未找到学生的学费标准: studentId={}, majorId={}, gradeLevel={}",
                            student.getId(), student.getMajor().getId(), gradeLevel);
                    skipCount++;
                    continue;
                }

                // 创建账单
                LocalDate dueDate = calculateDueDate(academicYear);
                Double totalAmount = standard.getTotalFee();

                TuitionBill bill = TuitionBill.builder()
                        .student(student)
                        .academicYear(academicYear)
                        .tuitionFee(standard.getTuitionFee())
                        .accommodationFee(standard.getAccommodationFee())
                        .textbookFee(standard.getTextbookFee())
                        .otherFees(standard.getOtherFees())
                        .totalAmount(totalAmount)
                        .paidAmount(0.0)
                        .outstandingAmount(totalAmount)
                        .status(BillStatus.UNPAID)
                        .dueDate(dueDate)
                        .generatedAt(LocalDateTime.now())
                        .build();

                bills.add(bill);
                successCount++;

                // 每1000条批量保存一次
                if (bills.size() >= 1000) {
                    billRepository.saveAll(bills);
                    bills.clear();
                }
            } catch (Exception e) {
                log.error("生成账单失败: studentId=" + student.getId(), e);
                skipCount++;
            }
        }

        // 保存剩余的账单
        if (!bills.isEmpty()) {
            billRepository.saveAll(bills);
        }

        log.info("批量生成账单完成: 成功={}, 跳过={}", successCount, skipCount);

        // 返回已生成的账单
        List<TuitionBill> generatedBills = billRepository.findByAcademicYear(academicYear);
        return tuitionConverter.toTuitionBillDTOList(generatedBills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TuitionBillDTO> getStudentBills(Long studentId) {
        List<TuitionBill> bills = billRepository.findByStudentId(studentId);
        return tuitionConverter.toTuitionBillDTOList(bills);
    }

    @Override
    @Transactional(readOnly = true)
    public TuitionBillDTO getBillDetail(Long billId) {
        TuitionBill bill = findBillById(billId);
        List<TuitionPayment> payments = paymentRepository.findByBillId(billId);
        
        TuitionBillDTO dto = tuitionConverter.toTuitionBillDTO(bill);
        dto.setPayments(tuitionConverter.toTuitionPaymentDTOList(payments));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TuitionBillDTO> searchBills(TuitionBillQueryDTO query, Pageable pageable) {
        // 将空字符串转换为null，避免查询条件错误
        String academicYear = (query.getAcademicYear() != null && query.getAcademicYear().trim().isEmpty()) 
                ? null : query.getAcademicYear();
        String studentNo = (query.getStudentNo() != null && query.getStudentNo().trim().isEmpty()) 
                ? null : query.getStudentNo();
        String studentName = (query.getStudentName() != null && query.getStudentName().trim().isEmpty()) 
                ? null : query.getStudentName();
        
        Page<TuitionBill> bills = billRepository.searchBills(
                academicYear,
                query.getStatus(),
                studentNo,
                studentName,
                pageable
        );
        return bills.map(tuitionConverter::toTuitionBillDTO);
    }

    @Override
    public List<TuitionBillDTO> getOverdueBills() {
        List<TuitionBill> bills = billRepository.findOverdueBills(LocalDate.now());
        return tuitionConverter.toTuitionBillDTOList(bills);
    }

    // ==================== 缴费记录管理 ====================

    @Override
    @Transactional
    public TuitionPaymentDTO createPayment(Long studentId, CreateTuitionPaymentRequest request) {
        log.info("创建缴费记录: studentId={}, billId={}, amount={}",
                studentId, request.getBillId(), request.getAmount());

        // 1. 验证账单
        TuitionBill bill = findBillById(request.getBillId());
        if (!bill.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 验证金额
        if (request.getAmount() > bill.getOutstandingAmount()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_EXCEEDS_OUTSTANDING);
        }

        // 3. 创建缴费记录
        String paymentNo = generatePaymentNo();
        TuitionPayment payment = TuitionPayment.builder()
                .bill(bill)
                .paymentNo(paymentNo)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .remark(request.getRemark())
                .build();

        payment = paymentRepository.save(payment);

        // 4. 模拟支付成功（实际应该对接支付网关）
        payment.markAsSuccess();
        payment = paymentRepository.save(payment);

        // 5. 更新账单
        bill.updatePayment(request.getAmount());
        billRepository.save(bill);

        log.info("缴费成功: paymentId={}, paymentNo={}", payment.getId(), paymentNo);

        // 6. 发送通知
        // TODO: 实现通知功能
        // try {
        //     notificationService.notifyPaymentSuccess(payment);
        // } catch (Exception e) {
        //     log.error("发送缴费通知失败", e);
        // }

        return tuitionConverter.toTuitionPaymentDTO(payment);
    }

    @Override
    @Transactional
    public TuitionPaymentDTO recordOfflinePayment(Long billId, RecordPaymentRequest request, Long operatorId) {
        log.info("录入线下缴费: billId={}, amount={}, operatorId={}",
                billId, request.getAmount(), operatorId);

        // 1. 验证账单
        TuitionBill bill = findBillById(billId);

        // 2. 验证金额
        if (request.getAmount() > bill.getOutstandingAmount()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_EXCEEDS_OUTSTANDING);
        }

        // 3. 创建缴费记录
        String paymentNo = generatePaymentNo();
        TuitionPayment payment = TuitionPayment.builder()
                .bill(bill)
                .paymentNo(paymentNo)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.SUCCESS)
                .transactionId(request.getTransactionId())
                .paidAt(request.getPaidAt() != null ? request.getPaidAt() : LocalDateTime.now())
                .operatorId(operatorId)
                .remark(request.getRemark())
                .build();

        payment = paymentRepository.save(payment);

        // 4. 更新账单
        bill.updatePayment(request.getAmount());
        billRepository.save(bill);

        log.info("线下缴费录入成功: paymentId={}, paymentNo={}", payment.getId(), paymentNo);

        return tuitionConverter.toTuitionPaymentDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TuitionPaymentDTO> getPayments(PaymentQueryDTO query, Pageable pageable) {
        // 将空字符串转换为null，避免查询条件错误
        String academicYear = (query.getAcademicYear() != null && query.getAcademicYear().trim().isEmpty()) 
                ? null : query.getAcademicYear();
        String studentNo = (query.getStudentNo() != null && query.getStudentNo().trim().isEmpty()) 
                ? null : query.getStudentNo();
        String paymentNo = (query.getPaymentNo() != null && query.getPaymentNo().trim().isEmpty()) 
                ? null : query.getPaymentNo();
        
        Page<TuitionPayment> payments = paymentRepository.searchPayments(
                academicYear,
                query.getStatus(),
                studentNo,
                paymentNo,
                pageable
        );
        return payments.map(tuitionConverter::toTuitionPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TuitionPaymentDTO> getStudentPayments(Long studentId) {
        List<TuitionPayment> payments = paymentRepository.findByStudentId(studentId);
        return tuitionConverter.toTuitionPaymentDTOList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public TuitionPaymentDTO getPaymentDetail(Long paymentId) {
        TuitionPayment payment = findPaymentById(paymentId);
        return tuitionConverter.toTuitionPaymentDTO(payment);
    }

    @Override
    public byte[] generateReceipt(Long paymentId) {
        // TODO: 实现电子收据生成（PDF）
        log.info("生成电子收据: paymentId={}", paymentId);
        findPaymentById(paymentId);
        
        // 这里应该使用PDF库（如iText）生成收据
        // 暂时返回空字节数组
        return new byte[0];
    }

    // ==================== 缴费提醒 ====================

    @Override
    @Transactional
    public void sendPaymentReminder(Long billId) {
        log.info("发送欠费提醒: billId={}", billId);

        TuitionBill bill = findBillById(billId);

        if (bill.getStatus() == BillStatus.PAID) {
            log.info("账单已支付，无需提醒: billId={}", billId);
            return;
        }

        // 创建提醒记录
        String content = String.format(
                "尊敬的%s同学，您还有%.2f元学费未缴纳，请在%s前完成缴费。",
                bill.getStudent().getName(),
                bill.getOutstandingAmount(),
                bill.getDueDate()
        );

        PaymentReminder reminder = PaymentReminder.builder()
                .bill(bill)
                .reminderType("NOTIFICATION")
                .sentAt(LocalDateTime.now())
                .content(content)
                .success(true)
                .build();

        reminderRepository.save(reminder);

        // 发送站内通知
        // TODO: 实现通知功能
        // try {
        //     notificationService.notifyPaymentReminder(bill);
        // } catch (Exception e) {
        //     log.error("发送欠费提醒失败", e);
        //     reminder.setSuccess(false);
        //     reminder.setFailureReason(e.getMessage());
        //     reminderRepository.save(reminder);
        // }
    }

    @Override
    @Transactional
    public void batchSendPaymentReminders() {
        log.info("批量发送欠费提醒");

        List<TuitionBill> overdueBills = billRepository.findOverdueBills(LocalDate.now());
        int successCount = 0;
        int failCount = 0;

        for (TuitionBill bill : overdueBills) {
            try {
                sendPaymentReminder(bill.getId());
                successCount++;
            } catch (Exception e) {
                log.error("发送提醒失败: billId=" + bill.getId(), e);
                failCount++;
            }
        }

        log.info("批量发送提醒完成: 成功={}, 失败={}", successCount, failCount);
    }

    // ==================== 内部方法 ====================

    @Override
    public TuitionBill findBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TUITION_BILL_NOT_FOUND));
    }

    @Override
    public TuitionPayment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public TuitionStandard findStandardById(Long standardId) {
        return standardRepository.findById(standardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TUITION_STANDARD_NOT_FOUND));
    }

    /**
     * 计算年级
     */
    private Integer calculateGradeLevel(Integer enrollmentYear, String academicYear) {
        // 从学年中提取起始年份
        Integer academicStartYear = Integer.parseInt(academicYear.split("-")[0]);
        return academicStartYear - enrollmentYear + 1;
    }

    /**
     * 计算缴费截止日期
     */
    private LocalDate calculateDueDate(String academicYear) {
        // 默认截止日期为学年开始后两个月（如2024-2025学年，截止日期为2024-11-30）
        Integer year = Integer.parseInt(academicYear.split("-")[0]);
        return LocalDate.of(year, 11, 30);
    }

    /**
     * 生成缴费单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

