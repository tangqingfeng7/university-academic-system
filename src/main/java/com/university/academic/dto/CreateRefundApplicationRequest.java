package com.university.academic.dto;

import com.university.academic.entity.tuition.RefundMethod;
import com.university.academic.entity.tuition.RefundType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 创建退费申请请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundApplicationRequest {

    /**
     * 缴费记录ID
     */
    @NotNull(message = "缴费记录ID不能为空")
    private Long paymentId;

    /**
     * 退费金额
     */
    @NotNull(message = "退费金额不能为空")
    @DecimalMin(value = "0.01", message = "退费金额必须大于0")
    private Double refundAmount;

    /**
     * 退费原因
     */
    @NotBlank(message = "退费原因不能为空")
    @Size(max = 500, message = "退费原因不能超过500字")
    private String reason;

    /**
     * 退费类型
     */
    @NotNull(message = "退费类型不能为空")
    private RefundType refundType;

    /**
     * 退费方式
     */
    @NotNull(message = "退费方式不能为空")
    private RefundMethod refundMethod;

    /**
     * 银行账号（退费到银行卡时必填）
     */
    private String bankAccount;

    /**
     * 附件（证明材料）
     */
    private MultipartFile attachment;
}

