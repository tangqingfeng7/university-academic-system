package com.university.academic.service;

import com.university.academic.dto.FinancialReportDTO;
import com.university.academic.dto.PaymentStatisticsDTO;

/**
 * 学费统计服务接口
 *
 * @author Academic System Team
 */
public interface TuitionStatisticsService {

    /**
     * 统计缴费率
     *
     * @param academicYear  学年
     * @param departmentId 院系ID（可选）
     * @return 缴费统计
     */
    PaymentStatisticsDTO getPaymentRate(String academicYear, Long departmentId);

    /**
     * 生成财务报表
     *
     * @param academicYear 学年
     * @return 财务报表
     */
    FinancialReportDTO generateFinancialReport(String academicYear);

    /**
     * 导出财务报表（Excel）
     *
     * @param academicYear 学年
     * @return Excel文件字节数组
     */
    byte[] exportFinancialReport(String academicYear);

    /**
     * 导出账单列表（Excel）
     *
     * @param academicYear 学年
     * @return Excel文件字节数组
     */
    byte[] exportBillList(String academicYear);
}

