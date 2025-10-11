package com.university.academic.dto;

import com.university.academic.entity.tuition.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 财务报表DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportDTO {

    /**
     * 报表生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 总收入
     */
    private Double totalIncome;

    /**
     * 学费收入
     */
    private Double tuitionIncome;

    /**
     * 住宿费收入
     */
    private Double accommodationIncome;

    /**
     * 教材费收入
     */
    private Double textbookIncome;

    /**
     * 其他费用收入
     */
    private Double otherIncome;

    /**
     * 各支付方式统计
     */
    private Map<PaymentMethod, PaymentMethodStatistics> paymentMethodStatistics;

    /**
     * 月度收入统计
     */
    private List<MonthlyIncomeStatistics> monthlyStatistics;

    /**
     * 支付方式统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodStatistics {
        private PaymentMethod method;
        private String methodDescription;
        private Long count;
        private Double amount;
        private Double percentage;
    }

    /**
     * 月度收入统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyIncomeStatistics {
        private Integer year;
        private Integer month;
        private Long paymentCount;
        private Double amount;
    }
}

