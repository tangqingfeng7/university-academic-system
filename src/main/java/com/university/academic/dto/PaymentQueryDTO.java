package com.university.academic.dto;

import com.university.academic.entity.tuition.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缴费记录查询DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueryDTO {

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 缴费单号
     */
    private String paymentNo;
}

