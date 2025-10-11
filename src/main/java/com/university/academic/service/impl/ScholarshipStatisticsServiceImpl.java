package com.university.academic.service.impl;

import com.university.academic.dto.AwardStatisticsDTO;
import com.university.academic.entity.ScholarshipAward;
import com.university.academic.entity.Student;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ScholarshipAwardRepository;
import com.university.academic.service.ScholarshipStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 奖学金统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipStatisticsServiceImpl implements ScholarshipStatisticsService {
    
    private final ScholarshipAwardRepository awardRepository;
    
    /**
     * 获奖分布统计
     */
    @Override
    @Transactional(readOnly = true)
    public AwardStatisticsDTO getAwardDistribution(String academicYear) {
        log.debug("获奖分布统计: academicYear={}", academicYear);
        
        List<ScholarshipAward> awards = awardRepository.findByAcademicYear(academicYear);
        
        if (awards.isEmpty()) {
            return createEmptyStatistics(academicYear);
        }
        
        return buildStatistics(academicYear, awards);
    }
    
    /**
     * 按专业统计
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getStatisticsByMajor(String academicYear, Long departmentId) {
        log.debug("按专业统计: academicYear={}, departmentId={}", academicYear, departmentId);
        
        List<ScholarshipAward> awards = awardRepository.findByAcademicYear(academicYear);
        
        // 如果指定了院系，则过滤
        if (departmentId != null) {
            awards = awards.stream()
                    .filter(award -> award.getStudent().getMajor().getDepartment().getId().equals(departmentId))
                    .collect(Collectors.toList());
        }
        
        return awards.stream()
                .collect(Collectors.groupingBy(
                        award -> award.getStudent().getMajor().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
    
    /**
     * 按年级统计
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Integer> getStatisticsByGrade(String academicYear) {
        log.debug("按年级统计: academicYear={}", academicYear);
        
        List<ScholarshipAward> awards = awardRepository.findByAcademicYear(academicYear);
        
        return awards.stream()
                .collect(Collectors.groupingBy(
                        award -> calculateGrade(award.getStudent()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
    
    /**
     * 生成统计报告（Excel）
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] generateStatisticsReport(String academicYear) {
        log.info("生成统计报告: academicYear={}", academicYear);
        
        AwardStatisticsDTO statistics = getAwardDistribution(academicYear);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建汇总页
            createSummarySheet(workbook, statistics);
            
            // 创建院系分布页
            createDepartmentSheet(workbook, statistics);
            
            // 创建专业分布页
            createMajorSheet(workbook, statistics);
            
            // 创建年级分布页
            createGradeSheet(workbook, statistics);
            
            // 创建奖学金等级分布页
            createLevelSheet(workbook, statistics);
            
            workbook.write(out);
            log.info("统计报告生成成功");
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("生成统计报告失败", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED);
        }
    }
    
    /**
     * 获取年度对比统计
     */
    @Override
    @Transactional(readOnly = true)
    public List<AwardStatisticsDTO> getYearlyComparison(String startYear, String endYear) {
        log.debug("年度对比统计: startYear={}, endYear={}", startYear, endYear);
        
        List<AwardStatisticsDTO> comparison = new ArrayList<>();
        
        // 解析学年（格式：2023-2024）
        int start = Integer.parseInt(startYear.split("-")[0]);
        int end = Integer.parseInt(endYear.split("-")[0]);
        
        for (int year = start; year <= end; year++) {
            String academicYear = year + "-" + (year + 1);
            AwardStatisticsDTO stats = getAwardDistribution(academicYear);
            comparison.add(stats);
        }
        
        return comparison;
    }
    
    /**
     * 构建统计数据
     */
    private AwardStatisticsDTO buildStatistics(String academicYear, List<ScholarshipAward> awards) {
        // 统计总人数和总金额
        int totalAwardees = awards.size();
        double totalAmount = awards.stream()
                .mapToDouble(ScholarshipAward::getAmount)
                .sum();
        
        // 按院系统计
        Map<String, Integer> byDepartment = awards.stream()
                .collect(Collectors.groupingBy(
                        award -> award.getStudent().getMajor().getDepartment().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // 按专业统计
        Map<String, Integer> byMajor = awards.stream()
                .collect(Collectors.groupingBy(
                        award -> award.getStudent().getMajor().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // 按年级统计
        Map<Integer, Integer> byGrade = awards.stream()
                .collect(Collectors.groupingBy(
                        award -> calculateGrade(award.getStudent()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // 按奖学金等级统计
        Map<String, Integer> byLevel = awards.stream()
                .collect(Collectors.groupingBy(
                        award -> award.getScholarship().getLevel().getDescription(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // 按奖学金类型统计
        Map<String, Integer> byScholarship = awards.stream()
                .collect(Collectors.groupingBy(
                        award -> award.getScholarship().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        return AwardStatisticsDTO.builder()
                .academicYear(academicYear)
                .totalAwardees(totalAwardees)
                .totalAmount(totalAmount)
                .byDepartment(byDepartment)
                .byMajor(byMajor)
                .byGrade(byGrade)
                .byLevel(byLevel)
                .byScholarship(byScholarship)
                .build();
    }
    
    /**
     * 创建空统计数据
     */
    private AwardStatisticsDTO createEmptyStatistics(String academicYear) {
        return AwardStatisticsDTO.builder()
                .academicYear(academicYear)
                .totalAwardees(0)
                .totalAmount(0.0)
                .byDepartment(new HashMap<>())
                .byMajor(new HashMap<>())
                .byGrade(new HashMap<>())
                .byLevel(new HashMap<>())
                .byScholarship(new HashMap<>())
                .build();
    }
    
    /**
     * 创建汇总页
     */
    private void createSummarySheet(Workbook workbook, AwardStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("统计汇总");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(statistics.getAcademicYear() + " 学年奖学金统计报告");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // 空行
        
        // 总体统计
        createDataRow(sheet, rowNum++, "总获奖人数", statistics.getTotalAwardees() + "人", headerStyle, dataStyle);
        createDataRow(sheet, rowNum++, "总奖学金金额", String.format("%.2f元", statistics.getTotalAmount()), headerStyle, dataStyle);
        
        // 设置列宽
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 8000);
    }
    
    /**
     * 创建院系分布页
     */
    private void createDepartmentSheet(Workbook workbook, AwardStatisticsDTO statistics) {
        createDistributionSheet(workbook, "院系分布", statistics.getByDepartment());
    }
    
    /**
     * 创建专业分布页
     */
    private void createMajorSheet(Workbook workbook, AwardStatisticsDTO statistics) {
        createDistributionSheet(workbook, "专业分布", statistics.getByMajor());
    }
    
    /**
     * 创建年级分布页
     */
    private void createGradeSheet(Workbook workbook, AwardStatisticsDTO statistics) {
        Map<String, Integer> gradeMap = statistics.getByGrade().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey() + "年级",
                        Map.Entry::getValue
                ));
        createDistributionSheet(workbook, "年级分布", gradeMap);
    }
    
    /**
     * 创建奖学金等级分布页
     */
    private void createLevelSheet(Workbook workbook, AwardStatisticsDTO statistics) {
        createDistributionSheet(workbook, "等级分布", statistics.getByLevel());
    }
    
    /**
     * 创建分布统计页（通用方法）
     */
    private void createDistributionSheet(Workbook workbook, String sheetName, Map<String, Integer> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 表头
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "类别", headerStyle);
        createCell(headerRow, 1, "获奖人数", headerStyle);
        createCell(headerRow, 2, "占比", headerStyle);
        
        // 计算总数
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        
        // 数据行
        int rowNum = 1;
        // 按人数降序排序
        List<Map.Entry<String, Integer>> sortedData = data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedData) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, entry.getKey(), dataStyle);
            createCell(row, 1, entry.getValue(), dataStyle);
            double percentage = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            createCell(row, 2, String.format("%.2f%%", percentage), dataStyle);
        }
        
        // 合计行
        Row totalRow = sheet.createRow(rowNum);
        createCell(totalRow, 0, "合计", headerStyle);
        createCell(totalRow, 1, total, headerStyle);
        createCell(totalRow, 2, "100.00%", headerStyle);
        
        // 设置列宽
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
    }
    
    /**
     * 创建数据行
     */
    private void createDataRow(Sheet sheet, int rowNum, String label, String value, 
                               CellStyle headerStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        createCell(row, 0, label, headerStyle);
        createCell(row, 1, value, dataStyle);
    }
    
    /**
     * 计算学生年级
     */
    private Integer calculateGrade(Student student) {
        int currentYear = java.time.Year.now().getValue();
        return currentYear - student.getEnrollmentYear() + 1;
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
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    /**
     * 创建单元格
     */
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        
        cell.setCellStyle(style);
    }
}

