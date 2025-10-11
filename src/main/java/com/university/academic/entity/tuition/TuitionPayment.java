package com.university.academic.entity.tuition;

import com.university.academic.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 缴费记录实体类
 * 记录学生的学费缴纳记录
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "tuition_payment", indexes = {
    @Index(name = "idx_payment_bill", columnList = "bill_id"),
    @Index(name = "idx_payment_no", columnList = "payment_no"),
    @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionPayment extends BaseEntity {

    /**
     * 学费账单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private TuitionBill bill;

    /**
     * 缴费单号
     */
    @Column(name = "payment_no", nullable = false, unique = true, length = 50)
    private String paymentNo;

    /**
     * 缴费金额
     */
    @Column(nullable = false)
    private Double amount;

    /**
     * 支付方式
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    /**
     * 支付状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 第三方交易号
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * 实际支付时间
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * 电子收据URL
     */
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    /**
     * 操作员ID（线下缴费时记录）
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 备注
     */
    @Column(columnDefinition = "TEXT")
    private String remark;

    /**
     * 标记支付成功
     */
    public void markAsSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 标记支付失败
     */
    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    /**
     * 标记已退款
     */
    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
}

