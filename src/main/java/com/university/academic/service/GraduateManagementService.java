package com.university.academic.service;

import com.university.academic.dto.GraduationAuditDTO;
import com.university.academic.dto.converter.GraduationAuditConverter;
import com.university.academic.entity.GraduationAudit;
import com.university.academic.repository.GraduationAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 毕业生管理服务类
 * 提供毕业生名单查询、导出、统计等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraduateManagementService {

    private final GraduationAuditRepository graduationAuditRepository;
    private final GraduationAuditConverter graduationAuditConverter;

    /**
     * 查询毕业生名单
     * 查询指定年份通过审核的学生
     *
     * @param year 毕业年份
     * @return 毕业生列表
     */
    @Transactional(readOnly = true)
    public List<GraduationAuditDTO> getGraduateList(Integer year) {
        log.info("查询毕业生名单: year={}", year);

        List<GraduationAudit> graduates = graduationAuditRepository
                .findByStatusAndAuditYearWithDetails(GraduationAudit.AuditStatus.PASS, year);

        log.info("查询到毕业生: count={}", graduates.size());

        return graduationAuditConverter.toDTOList(graduates);
    }

    /**
     * 导出毕业生名单（Excel格式）
     *
     * @param year 毕业年份
     * @return Excel文件字节数组
     */
    @Transactional(readOnly = true)
    public byte[] exportGraduateList(Integer year) throws IOException {
        log.info("导出毕业生名单: year={}", year);

        List<GraduationAuditDTO> graduates = getGraduateList(year);

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(year + "届毕业生名单");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"序号", "学号", "姓名", "专业", "总学分", "必修学分", 
                               "选修学分", "实践学分", "审核时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // 填充数据行
            CellStyle dataStyle = createDataStyle(workbook);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            int rowIndex = 1;
            for (GraduationAuditDTO graduate : graduates) {
                Row row = sheet.createRow(rowIndex);

                row.createCell(0).setCellValue(rowIndex);
                row.createCell(1).setCellValue(graduate.getStudentNo());
                row.createCell(2).setCellValue(graduate.getStudentName());
                row.createCell(3).setCellValue(graduate.getMajorName());
                row.createCell(4).setCellValue(graduate.getTotalCredits());
                row.createCell(5).setCellValue(graduate.getRequiredCredits());
                row.createCell(6).setCellValue(graduate.getElectiveCredits());
                row.createCell(7).setCellValue(graduate.getPracticalCredits());
                row.createCell(8).setCellValue(
                        graduate.getAuditedAt() != null ? 
                        graduate.getAuditedAt().format(formatter) : "");

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
            log.info("毕业生名单导出完成: count={}", graduates.size());

            return out.toByteArray();
        }
    }

    /**
     * 统计毕业生信息
     *
     * @param year 毕业年份
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGraduateStatistics(Integer year) {
        log.info("统计毕业生信息: year={}", year);

        List<GraduationAuditDTO> graduates = getGraduateList(year);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("year", year);
        statistics.put("totalCount", graduates.size());

        // 按专业统计
        Map<String, Long> byMajor = graduates.stream()
                .collect(Collectors.groupingBy(
                        GraduationAuditDTO::getMajorName,
                        Collectors.counting()
                ));
        statistics.put("byMajor", byMajor);

        // 平均学分统计
        double avgTotalCredits = graduates.stream()
                .mapToDouble(GraduationAuditDTO::getTotalCredits)
                .average()
                .orElse(0.0);
        statistics.put("avgTotalCredits", avgTotalCredits);

        double avgRequiredCredits = graduates.stream()
                .mapToDouble(GraduationAuditDTO::getRequiredCredits)
                .average()
                .orElse(0.0);
        statistics.put("avgRequiredCredits", avgRequiredCredits);

        double avgElectiveCredits = graduates.stream()
                .mapToDouble(GraduationAuditDTO::getElectiveCredits)
                .average()
                .orElse(0.0);
        statistics.put("avgElectiveCredits", avgElectiveCredits);

        double avgPracticalCredits = graduates.stream()
                .mapToDouble(GraduationAuditDTO::getPracticalCredits)
                .average()
                .orElse(0.0);
        statistics.put("avgPracticalCredits", avgPracticalCredits);

        log.info("毕业生统计完成: totalCount={}", graduates.size());

        return statistics;
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
        font.setFontHeightInPoints((short) 12);
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

