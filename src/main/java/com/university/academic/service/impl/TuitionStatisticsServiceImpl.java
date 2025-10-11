package com.university.academic.service.impl;

import com.university.academic.dto.FinancialReportDTO;
import com.university.academic.dto.PaymentStatisticsDTO;
import com.university.academic.entity.tuition.BillStatus;
import com.university.academic.entity.tuition.PaymentMethod;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TuitionBillRepository;
import com.university.academic.repository.TuitionPaymentRepository;
import com.university.academic.service.TuitionStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 学费统计服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionStatisticsServiceImpl implements TuitionStatisticsService {

    private final TuitionBillRepository billRepository;
    private final TuitionPaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    @Override
    public PaymentStatisticsDTO getPaymentRate(String academicYear, Long departmentId) {
        log.info("统计缴费率: academicYear={}, departmentId={}", academicYear, departmentId);

        // 获取在校学生总数
        Long totalStudents = studentRepository.countByDeletedFalse();

        // 获取统计数据
        List<Object[]> statistics;
        if (departmentId != null) {
            statistics = billRepository.getPaymentStatisticsByDepartment(academicYear, departmentId);
        } else {
            statistics = billRepository.getPaymentStatisticsByYear(academicYear);
        }

        // 计算总数
        Long totalBills = 0L;
        Double totalAmount = 0.0;
        Double paidAmount = 0.0;
        Map<BillStatus, Long> statusCount = new HashMap<>();

        for (Object[] row : statistics) {
            BillStatus status = (BillStatus) row[0];
            Long count = (Long) row[1];
            Double total = (Double) row[2];
            Double paid = (Double) row[3];

            totalBills += count;
            totalAmount += (total != null ? total : 0.0);
            paidAmount += (paid != null ? paid : 0.0);
            statusCount.put(status, count);
        }

        // 计算缴费率
        Double paymentRate = totalAmount > 0 ? (paidAmount / totalAmount * 100) : 0.0;

        // 获取院系统计
        List<PaymentStatisticsDTO.DepartmentPaymentStatistics> departmentStatistics = new ArrayList<>();
        List<Object[]> deptStats = billRepository.getStatisticsByDepartment(academicYear);
        for (Object[] row : deptStats) {
            Long deptId = (Long) row[0];
            String deptName = (String) row[1];
            Long studentCount = (Long) row[2];
            Double deptTotal = (Double) row[3];
            Double deptPaid = (Double) row[4];
            Double deptRate = deptTotal > 0 ? (deptPaid / deptTotal * 100) : 0.0;

            departmentStatistics.add(PaymentStatisticsDTO.DepartmentPaymentStatistics.builder()
                    .departmentId(deptId)
                    .departmentName(deptName)
                    .studentCount(studentCount)
                    .totalAmount(deptTotal != null ? deptTotal : 0.0)
                    .paidAmount(deptPaid != null ? deptPaid : 0.0)
                    .paymentRate(Math.round(deptRate * 100.0) / 100.0)
                    .build());
        }

        // 获取专业统计
        List<PaymentStatisticsDTO.MajorPaymentStatistics> majorStatistics = new ArrayList<>();
        List<Object[]> majorStats = billRepository.getStatisticsByMajor(academicYear);
        for (Object[] row : majorStats) {
            Long majorId = (Long) row[0];
            String majorName = (String) row[1];
            Long studentCount = (Long) row[2];
            Double majorTotal = (Double) row[3];
            Double majorPaid = (Double) row[4];
            Double majorRate = majorTotal > 0 ? (majorPaid / majorTotal * 100) : 0.0;

            majorStatistics.add(PaymentStatisticsDTO.MajorPaymentStatistics.builder()
                    .majorId(majorId)
                    .majorName(majorName)
                    .studentCount(studentCount)
                    .totalAmount(majorTotal != null ? majorTotal : 0.0)
                    .paidAmount(majorPaid != null ? majorPaid : 0.0)
                    .paymentRate(Math.round(majorRate * 100.0) / 100.0)
                    .build());
        }

        return PaymentStatisticsDTO.builder()
                .academicYear(academicYear)
                .totalStudents(totalStudents)
                .totalBills(totalBills)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(totalAmount - paidAmount)
                .paymentRate(Math.round(paymentRate * 100.0) / 100.0)
                .billStatusCount(statusCount)
                .departmentStatistics(departmentStatistics)
                .majorStatistics(majorStatistics)
                .build();
    }

    @Override
    public FinancialReportDTO generateFinancialReport(String academicYear) {
        log.info("生成财务报表: academicYear={}", academicYear);

        // 获取学年时间范围
        Integer startYear = Integer.parseInt(academicYear.split("-")[0]);
        Integer endYear = startYear + 1;
        LocalDateTime startTime = LocalDateTime.of(startYear, 9, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(endYear, 8, 31, 23, 59);

        // 统计总收入
        Double totalIncome = paymentRepository.sumAmountByAcademicYear(academicYear);
        if (totalIncome == null) {
            totalIncome = 0.0;
        }

        // 统计各支付方式
        List<Object[]> methodStats = paymentRepository.getPaymentMethodStatistics(startTime, endTime);
        Map<PaymentMethod, FinancialReportDTO.PaymentMethodStatistics> methodStatistics = new HashMap<>();

        for (Object[] row : methodStats) {
            PaymentMethod method = (PaymentMethod) row[0];
            Long count = (Long) row[1];
            Double amount = (Double) row[2];
            Double percentage = totalIncome > 0 ? (amount / totalIncome * 100) : 0.0;

            methodStatistics.put(method, FinancialReportDTO.PaymentMethodStatistics.builder()
                    .method(method)
                    .methodDescription(method.getDescription())
                    .count(count)
                    .amount(amount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        // 统计月度收入
        List<FinancialReportDTO.MonthlyIncomeStatistics> monthlyStatistics = new ArrayList<>();
        for (int month = 9; month <= 12; month++) {
            addMonthlyStatistics(monthlyStatistics, startYear, month, paymentRepository);
        }
        for (int month = 1; month <= 8; month++) {
            addMonthlyStatistics(monthlyStatistics, endYear, month, paymentRepository);
        }

        return FinancialReportDTO.builder()
                .generatedAt(LocalDateTime.now())
                .academicYear(academicYear)
                .totalIncome(totalIncome)
                .tuitionIncome(0.0) // TODO: 细化统计
                .accommodationIncome(0.0)
                .textbookIncome(0.0)
                .otherIncome(0.0)
                .paymentMethodStatistics(methodStatistics)
                .monthlyStatistics(monthlyStatistics)
                .build();
    }

    @Override
    public byte[] exportFinancialReport(String academicYear) {
        // TODO: 实现Excel导出
        log.info("导出财务报表: academicYear={}", academicYear);
        generateFinancialReport(academicYear);
        
        // 这里应该使用Apache POI生成Excel
        // 暂时返回空字节数组
        return new byte[0];
    }

    @Override
    public byte[] exportBillList(String academicYear) {
        // TODO: 实现Excel导出
        log.info("导出账单列表: academicYear={}", academicYear);
        
        // 这里应该使用Apache POI生成Excel
        // 暂时返回空字节数组
        return new byte[0];
    }

    /**
     * 添加月度统计
     */
    private void addMonthlyStatistics(
            List<FinancialReportDTO.MonthlyIncomeStatistics> list,
            Integer year,
            Integer month,
            TuitionPaymentRepository paymentRepository) {
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        Double amount = paymentRepository.sumAmountByPaidTimeBetween(startTime, endTime);
        if (amount == null) {
            amount = 0.0;
        }

        list.add(FinancialReportDTO.MonthlyIncomeStatistics.builder()
                .year(year)
                .month(month)
                .paymentCount(0L) // TODO: 添加数量统计
                .amount(amount)
                .build());
    }
}

