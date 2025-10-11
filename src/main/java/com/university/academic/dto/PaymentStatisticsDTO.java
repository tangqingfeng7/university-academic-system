package com.university.academic.dto;

import com.university.academic.entity.tuition.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 缴费统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsDTO {

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 总学生数
     */
    private Long totalStudents;

    /**
     * 已生成账单数
     */
    private Long totalBills;

    /**
     * 应收总额
     */
    private Double totalAmount;

    /**
     * 已收总额
     */
    private Double paidAmount;

    /**
     * 欠费总额
     */
    private Double outstandingAmount;

    /**
     * 缴费率（%）
     */
    private Double paymentRate;

    /**
     * 各状态账单数量
     */
    private Map<BillStatus, Long> billStatusCount;

    /**
     * 各专业缴费情况
     */
    private List<MajorPaymentStatistics> majorStatistics;

    /**
     * 各院系缴费情况
     */
    private List<DepartmentPaymentStatistics> departmentStatistics;

    /**
     * 专业缴费统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorPaymentStatistics {
        private Long majorId;
        private String majorName;
        private Long studentCount;
        private Double totalAmount;
        private Double paidAmount;
        private Double paymentRate;
    }

    /**
     * 院系缴费统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentPaymentStatistics {
        private Long departmentId;
        private String departmentName;
        private Long studentCount;
        private Double totalAmount;
        private Double paidAmount;
        private Double paymentRate;
    }
}

