package com.university.academic.dto;

import com.university.academic.entity.tuition.PaymentMethod;
import com.university.academic.entity.tuition.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 缴费记录DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionPaymentDTO {

    /**
     * 缴费记录ID
     */
    private Long id;

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 缴费单号
     */
    private String paymentNo;

    /**
     * 缴费金额
     */
    private Double amount;

    /**
     * 支付方式
     */
    private PaymentMethod method;

    /**
     * 支付方式描述
     */
    private String methodDescription;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 实际支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 电子收据URL
     */
    private String receiptUrl;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

