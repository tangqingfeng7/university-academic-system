package com.university.academic.dto;

import com.university.academic.entity.tuition.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建缴费记录请求（在线缴费）
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTuitionPaymentRequest {

    /**
     * 账单ID
     */
    @NotNull(message = "账单ID不能为空")
    private Long billId;

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
     * 备注
     */
    private String remark;
}

