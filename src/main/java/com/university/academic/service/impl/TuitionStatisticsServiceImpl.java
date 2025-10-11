package com.university.academic.service.impl;

import com.university.academic.dto.FinancialReportDTO;
import com.university.academic.dto.PaymentStatisticsDTO;
import com.university.academic.entity.tuition.BillStatus;
import com.university.academic.entity.tuition.PaymentMethod;
import com.university.academic.entity.tuition.TuitionBill;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TuitionBillRepository;
import com.university.academic.repository.TuitionPaymentRepository;
import com.university.academic.service.TuitionStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

        // 获取各项费用的细化统计
        Object[] feeBreakdown = billRepository.getFeeBreakdownByAcademicYear(academicYear);
        Double tuitionIncome = feeBreakdown != null && feeBreakdown[0] != null ? (Double) feeBreakdown[0] : 0.0;
        Double accommodationIncome = feeBreakdown != null && feeBreakdown[1] != null ? (Double) feeBreakdown[1] : 0.0;
        Double textbookIncome = feeBreakdown != null && feeBreakdown[2] != null ? (Double) feeBreakdown[2] : 0.0;
        Double otherIncome = feeBreakdown != null && feeBreakdown[3] != null ? (Double) feeBreakdown[3] : 0.0;

        log.debug("费用细化统计: 学费={}, 住宿费={}, 教材费={}, 其他费用={}", 
                tuitionIncome, accommodationIncome, textbookIncome, otherIncome);

        return FinancialReportDTO.builder()
                .generatedAt(LocalDateTime.now())
                .academicYear(academicYear)
                .totalIncome(totalIncome)
                .tuitionIncome(tuitionIncome)
                .accommodationIncome(accommodationIncome)
                .textbookIncome(textbookIncome)
                .otherIncome(otherIncome)
                .paymentMethodStatistics(methodStatistics)
                .monthlyStatistics(monthlyStatistics)
                .build();
    }

    @Override
    public byte[] exportFinancialReport(String academicYear) {
        log.info("导出财务报表: academicYear={}", academicYear);
        
        FinancialReportDTO report = generateFinancialReport(academicYear);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建概览Sheet
            createSummarySheet(workbook, report);
            
            // 创建月度统计Sheet
            createMonthlySheet(workbook, report);
            
            // 创建支付方式统计Sheet
            createPaymentMethodSheet(workbook, report);
            
            workbook.write(out);
            log.info("财务报表导出完成: academicYear={}", academicYear);
            
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出财务报表失败: academicYear={}", academicYear, e);
            throw new RuntimeException("导出财务报表失败", e);
        }
    }

    @Override
    public byte[] exportBillList(String academicYear) {
        log.info("导出账单列表: academicYear={}", academicYear);
        
        List<TuitionBill> bills = billRepository.findByAcademicYear(academicYear);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(academicYear + "学年学费账单");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            String[] headers = {"序号", "学号", "姓名", "专业", "学费", "住宿费", 
                               "教材费", "其他费用", "应缴总额", "已缴金额", "欠费金额", 
                               "状态", "缴费截止日期"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据行
            CellStyle dataStyle = createDataStyle(workbook);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            int rowIndex = 1;
            for (TuitionBill bill : bills) {
                Row row = sheet.createRow(rowIndex);
                
                row.createCell(0).setCellValue(rowIndex);
                row.createCell(1).setCellValue(bill.getStudent().getStudentNo());
                row.createCell(2).setCellValue(bill.getStudent().getName());
                row.createCell(3).setCellValue(bill.getStudent().getMajor().getName());
                row.createCell(4).setCellValue(bill.getTuitionFee());
                row.createCell(5).setCellValue(bill.getAccommodationFee());
                row.createCell(6).setCellValue(bill.getTextbookFee());
                row.createCell(7).setCellValue(bill.getOtherFees() != null ? bill.getOtherFees() : 0.0);
                row.createCell(8).setCellValue(bill.getTotalAmount());
                row.createCell(9).setCellValue(bill.getPaidAmount());
                row.createCell(10).setCellValue(bill.getOutstandingAmount());
                row.createCell(11).setCellValue(bill.getStatus().getDescription());
                row.createCell(12).setCellValue(bill.getDueDate().format(dateFormatter));
                
                // 应用样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
                
                rowIndex++;
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            log.info("账单列表导出完成: count={}", bills.size());
            
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出账单列表失败: academicYear={}", academicYear, e);
            throw new RuntimeException("导出账单列表失败", e);
        }
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

        Long count = paymentRepository.countByPaidTimeBetween(startTime, endTime);
        if (count == null) {
            count = 0L;
        }

        list.add(FinancialReportDTO.MonthlyIncomeStatistics.builder()
                .year(year)
                .month(month)
                .paymentCount(count)
                .amount(amount)
                .build());
    }

    /**
     * 创建概览Sheet
     */
    private void createSummarySheet(Workbook workbook, FinancialReportDTO report) {
        Sheet sheet = workbook.createSheet("财务概览");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowIndex = 0;
        
        // 标题
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(report.getAcademicYear() + "学年财务报表");
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
        
        rowIndex++; // 空行
        
        // 总收入
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("总收入");
        row.createCell(1).setCellValue(String.format("%.2f 元", report.getTotalIncome()));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 学费收入
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("学费收入");
        row.createCell(1).setCellValue(String.format("%.2f 元", report.getTuitionIncome()));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 住宿费收入
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("住宿费收入");
        row.createCell(1).setCellValue(String.format("%.2f 元", report.getAccommodationIncome()));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 教材费收入
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("教材费收入");
        row.createCell(1).setCellValue(String.format("%.2f 元", report.getTextbookIncome()));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 其他收入
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("其他收入");
        row.createCell(1).setCellValue(String.format("%.2f 元", report.getOtherIncome()));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 生成时间
        rowIndex++;
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("生成时间");
        row.createCell(1).setCellValue(report.getGeneratedAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
        
        // 自动调整列宽
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * 创建月度统计Sheet
     */
    private void createMonthlySheet(Workbook workbook, FinancialReportDTO report) {
        Sheet sheet = workbook.createSheet("月度收入统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"年份", "月份", "缴费笔数", "收入金额（元）"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowIndex = 1;
        for (FinancialReportDTO.MonthlyIncomeStatistics stat : report.getMonthlyStatistics()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(stat.getYear());
            row.createCell(1).setCellValue(stat.getMonth() + "月");
            row.createCell(2).setCellValue(stat.getPaymentCount());
            row.createCell(3).setCellValue(stat.getAmount());
            
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建支付方式统计Sheet
     */
    private void createPaymentMethodSheet(Workbook workbook, FinancialReportDTO report) {
        Sheet sheet = workbook.createSheet("支付方式统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"支付方式", "使用次数", "金额（元）", "占比（%）"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowIndex = 1;
        for (FinancialReportDTO.PaymentMethodStatistics stat : 
                report.getPaymentMethodStatistics().values()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(stat.getMethodDescription());
            row.createCell(1).setCellValue(stat.getCount());
            row.createCell(2).setCellValue(stat.getAmount());
            row.createCell(3).setCellValue(stat.getPercentage());
            
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        
        return style;
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}

