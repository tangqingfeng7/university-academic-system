package com.university.academic.dto;

import com.university.academic.entity.tuition.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 录入线下缴费请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequest {

    /**
     * 缴费金额
     */
    @NotNull(message = "缴费金额不能为空")
    @DecimalMin(value = "0.01", message = "缴费金额必须大于0")
    private Double amount;

    /**
     * 支付方式
     */
    @NotNull(message = "支付方式不能为空")
    private PaymentMethod method;

    /**
     * 支付时间（可选，默认为当前时间）
     */
    private LocalDateTime paidAt;

    /**
     * 第三方交易号（如银行流水号）
     */
    private String transactionId;

    /**
     * 备注
     */
    private String remark;
}

