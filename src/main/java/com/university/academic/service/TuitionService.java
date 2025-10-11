package com.university.academic.service;

import com.university.academic.dto.*;
import com.university.academic.entity.tuition.TuitionBill;
import com.university.academic.entity.tuition.TuitionPayment;
import com.university.academic.entity.tuition.TuitionStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 学费缴纳服务接口
 *
 * @author Academic System Team
 */
public interface TuitionService {

    // ==================== 学费标准管理 ====================

    /**
     * 设置学费标准
     */
    TuitionStandardDTO setStandard(CreateTuitionStandardRequest request);

    /**
     * 更新学费标准
     */
    TuitionStandardDTO updateStandard(Long id, CreateTuitionStandardRequest request);

    /**
     * 查询学费标准
     */
    TuitionStandardDTO getStandard(Long id);

    /**
     * 查询专业的学费标准
     */
    List<TuitionStandardDTO> getStandardsByMajor(Long majorId);

    /**
     * 查询学年的学费标准
     */
    List<TuitionStandardDTO> getStandardsByAcademicYear(String academicYear);

    /**
     * 分页查询学费标准（支持多条件筛选）
     */
    Page<TuitionStandardDTO> getStandards(String academicYear, Long majorId, Integer gradeLevel, Boolean active, Pageable pageable);

    /**
     * 停用学费标准
     */
    void deactivateStandard(Long id);

    // ==================== 学费账单管理 ====================

    /**
     * 生成学费账单
     */
    TuitionBillDTO generateBill(Long studentId, String academicYear);

    /**
     * 批量生成学费账单
     */
    List<TuitionBillDTO> batchGenerateBills(String academicYear);

    /**
     * 查询学生的账单
     */
    List<TuitionBillDTO> getStudentBills(Long studentId);

    /**
     * 查询账单详情
     */
    TuitionBillDTO getBillDetail(Long billId);

    /**
     * 多条件查询账单
     */
    Page<TuitionBillDTO> searchBills(TuitionBillQueryDTO query, Pageable pageable);

    /**
     * 查询逾期账单
     */
    List<TuitionBillDTO> getOverdueBills();

    // ==================== 缴费记录管理 ====================

    /**
     * 在线缴费
     */
    TuitionPaymentDTO createPayment(Long studentId, CreateTuitionPaymentRequest request);

    /**
     * 录入线下缴费
     */
    TuitionPaymentDTO recordOfflinePayment(Long billId, RecordPaymentRequest request, Long operatorId);

    /**
     * 查询缴费记录
     */
    Page<TuitionPaymentDTO> getPayments(PaymentQueryDTO query, Pageable pageable);

    /**
     * 查询学生的缴费记录
     */
    List<TuitionPaymentDTO> getStudentPayments(Long studentId);

    /**
     * 查询缴费详情
     */
    TuitionPaymentDTO getPaymentDetail(Long paymentId);

    /**
     * 生成电子收据
     */
    byte[] generateReceipt(Long paymentId);

    // ==================== 缴费提醒 ====================

    /**
     * 发送欠费提醒
     */
    void sendPaymentReminder(Long billId);

    /**
     * 批量发送欠费提醒
     */
    void batchSendPaymentReminders();

    // ==================== 内部方法（供Service层调用） ====================

    /**
     * 查找账单实体
     */
    TuitionBill findBillById(Long billId);

    /**
     * 查找缴费记录实体
     */
    TuitionPayment findPaymentById(Long paymentId);

    /**
     * 查找学费标准实体
     */
    TuitionStandard findStandardById(Long standardId);
}

