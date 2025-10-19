package com.university.academic.service.attendance.impl;

import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Department;
import com.university.academic.entity.attendance.AttendanceStatistics;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.AttendanceException;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.DepartmentRepository;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import com.university.academic.service.attendance.AttendanceExportService;
import com.university.academic.service.attendance.AttendanceStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 考勤导出服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceExportServiceImpl implements AttendanceExportService {

    private final AttendanceStatisticsRepository statisticsRepository;
    private final CourseOfferingRepository offeringRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceStatisticsService statisticsService;
    private final com.university.academic.repository.SemesterRepository semesterRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCourseAttendance(Long offeringId) {
        log.info("导出课程考勤: offeringId={}", offeringId);

        CourseOffering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.OFFERING_NOT_FOUND));

        // 获取考勤统计数据
        List<AttendanceStatistics> statsList = statisticsRepository.findByOfferingIdWithStudent(offeringId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("考勤记录");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("《%s》课程考勤统计表", offering.getCourse().getName()));
            titleCell.setCellStyle(headerStyle);

            // 创建表头
            Row headerRow = sheet.createRow(2);
            String[] headers = {"学号", "姓名", "班级", "总课次", "出勤", "迟到", "早退", "请假", "旷课", "出勤率(%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowIndex = 3;
            for (AttendanceStatistics stats : statsList) {
                Row row = sheet.createRow(rowIndex++);
                
                row.createCell(0).setCellValue(stats.getStudent().getStudentNo());
                row.createCell(1).setCellValue(stats.getStudent().getName());
                row.createCell(2).setCellValue(stats.getStudent().getClassName() != null ? 
                        stats.getStudent().getClassName() : "");
                row.createCell(3).setCellValue(stats.getTotalClasses());
                row.createCell(4).setCellValue(stats.getPresentCount());
                row.createCell(5).setCellValue(stats.getLateCount());
                row.createCell(6).setCellValue(stats.getEarlyLeaveCount());
                row.createCell(7).setCellValue(stats.getLeaveCount());
                row.createCell(8).setCellValue(stats.getAbsentCount());
                row.createCell(9).setCellValue(stats.getAttendanceRate() != null ? 
                        stats.getAttendanceRate() : 0.0);

                // 应用数据样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出课程考勤失败", e);
            throw new AttendanceException(ErrorCode.EXPORT_ERROR, "导出失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("导出院系统计: departmentId={}, startDate={}, endDate={}", 
                departmentId, startDate, endDate);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.DEPARTMENT_NOT_FOUND));

        Map<String, Object> stats = statisticsService.getDepartmentStatistics(
                departmentId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("院系考勤统计");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 标题
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("%s 考勤统计报表", department.getName()));
            titleCell.setCellStyle(headerStyle);

            // 统计信息
            int rowIndex = 2;
            createInfoRow(sheet, rowIndex++, "统计时间", 
                    startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER), 
                    dataStyle);
            createInfoRow(sheet, rowIndex++, "考勤次数", stats.get("totalRecords").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "总学生人次", stats.get("totalStudents").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "出勤人次", stats.get("totalPresent").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "迟到人次", stats.get("totalLate").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "旷课人次", stats.get("totalAbsent").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "平均出勤率", stats.get("avgAttendanceRate") + "%", dataStyle);

            // 自动调整列宽
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出院系统计失败", e);
            throw new AttendanceException(ErrorCode.EXPORT_ERROR, "导出失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTeacherStatistics(Long teacherId, LocalDate startDate, LocalDate endDate) {
        log.info("导出教师统计: teacherId={}, startDate={}, endDate={}", 
                teacherId, startDate, endDate);

        Map<String, Object> stats = statisticsService.getTeacherStatistics(
                teacherId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("教师考勤统计");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("教师考勤统计表");
            titleCell.setCellStyle(headerStyle);

            int rowIndex = 2;
            createInfoRow(sheet, rowIndex++, "统计时间", 
                    startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER), 
                    dataStyle);
            createInfoRow(sheet, rowIndex++, "考勤次数", stats.get("totalRecords").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "总学生人次", stats.get("totalClasses").toString(), dataStyle);
            createInfoRow(sheet, rowIndex++, "平均出勤率", stats.get("avgAttendanceRate") + "%", dataStyle);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出教师统计失败", e);
            throw new AttendanceException(ErrorCode.EXPORT_ERROR, "导出失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlyReport(Long departmentId, Integer year, Integer month) {
        log.info("生成月度报告: departmentId={}, year={}, month={}", departmentId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 使用Excel简化实现（实际应使用PDF）
        return exportDepartmentStatistics(departmentId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateSemesterReport(Long semesterId) {
        log.info("生成学期报告: semesterId={}", semesterId);

        // 获取学期信息
        com.university.academic.entity.Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.OPERATION_FAILED, "学期不存在"));

        // 获取该学期所有开课计划
        List<CourseOffering> offerings = offeringRepository.findBySemesterId(semesterId);
        
        if (offerings.isEmpty()) {
            throw new AttendanceException(ErrorCode.OPERATION_FAILED, "该学期没有开课计划");
        }

        // 收集所有考勤统计数据
        Map<Long, List<AttendanceStatistics>> offeringStatsMap = new java.util.HashMap<>();
        for (CourseOffering offering : offerings) {
            List<AttendanceStatistics> stats = statisticsRepository.findByOfferingIdWithStudent(offering.getId());
            if (!stats.isEmpty()) {
                offeringStatsMap.put(offering.getId(), stats);
            }
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // 1. 创建总览表
            createOverviewSheet(workbook, semester, offerings, offeringStatsMap, titleStyle, headerStyle, dataStyle);

            // 2. 创建按课程统计表
            createCourseStatisticsSheet(workbook, offerings, offeringStatsMap, headerStyle, dataStyle);

            // 3. 创建按院系统计表
            createDepartmentStatisticsSheet(workbook, offerings, offeringStatsMap, headerStyle, dataStyle);

            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("生成学期报告失败", e);
            throw new AttendanceException(ErrorCode.EXPORT_ERROR, "生成学期报告失败");
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建信息行
     */
    private void createInfoRow(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建总览表
     */
    private void createOverviewSheet(Workbook workbook, com.university.academic.entity.Semester semester,
                                    List<CourseOffering> offerings,
                                    Map<Long, List<AttendanceStatistics>> offeringStatsMap,
                                    CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("学期考勤总览");

        // 标题
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(String.format("%s 考勤总览报告", semester.getSemesterName()));
        titleCell.setCellStyle(titleStyle);

        // 学期基本信息
        int rowIndex = 2;
        createInfoRow(sheet, rowIndex++, "学期", semester.getSemesterName(), dataStyle);
        createInfoRow(sheet, rowIndex++, "起止时间", 
                semester.getStartDate().format(DATE_FORMATTER) + " 至 " + 
                semester.getEndDate().format(DATE_FORMATTER), dataStyle);
        createInfoRow(sheet, rowIndex++, "报告生成时间", 
                LocalDate.now().format(DATE_FORMATTER), dataStyle);

        rowIndex++; // 空行

        // 统计汇总数据
        int totalCourses = offerings.size();
        int coursesWithAttendance = offeringStatsMap.size();
        long totalStudentRecords = 0;
        long totalPresent = 0;
        long totalLate = 0;
        long totalAbsent = 0;
        long totalLeave = 0;
        int totalClasses = 0;

        for (List<AttendanceStatistics> statsList : offeringStatsMap.values()) {
            totalStudentRecords += statsList.size();
            for (AttendanceStatistics stats : statsList) {
                totalPresent += stats.getPresentCount();
                totalLate += stats.getLateCount();
                totalAbsent += stats.getAbsentCount();
                totalLeave += stats.getLeaveCount();
                if (stats.getTotalClasses() > totalClasses) {
                    totalClasses = stats.getTotalClasses();
                }
            }
        }

        // 计算平均出勤率
        long totalRecords = totalPresent + totalLate + totalAbsent + totalLeave;
        double avgAttendanceRate = totalRecords > 0 
                ? (totalPresent * 100.0 / totalRecords) 
                : 0.0;

        // 显示统计信息
        Row statsTitleRow = sheet.createRow(rowIndex++);
        Cell statsTitleCell = statsTitleRow.createCell(0);
        statsTitleCell.setCellValue("整体统计");
        statsTitleCell.setCellStyle(headerStyle);

        createInfoRow(sheet, rowIndex++, "开课总数", String.valueOf(totalCourses), dataStyle);
        createInfoRow(sheet, rowIndex++, "有考勤课程数", String.valueOf(coursesWithAttendance), dataStyle);
        createInfoRow(sheet, rowIndex++, "学生选课人次", String.valueOf(totalStudentRecords), dataStyle);
        createInfoRow(sheet, rowIndex++, "平均考勤次数", String.valueOf(totalClasses), dataStyle);
        createInfoRow(sheet, rowIndex++, "出勤人次", String.valueOf(totalPresent), dataStyle);
        createInfoRow(sheet, rowIndex++, "迟到人次", String.valueOf(totalLate), dataStyle);
        createInfoRow(sheet, rowIndex++, "旷课人次", String.valueOf(totalAbsent), dataStyle);
        createInfoRow(sheet, rowIndex++, "请假人次", String.valueOf(totalLeave), dataStyle);
        createInfoRow(sheet, rowIndex++, "平均出勤率", 
                String.format("%.2f%%", avgAttendanceRate), dataStyle);

        // 自动调整列宽
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * 创建按课程统计表
     */
    private void createCourseStatisticsSheet(Workbook workbook, List<CourseOffering> offerings,
                                            Map<Long, List<AttendanceStatistics>> offeringStatsMap,
                                            CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("按课程统计");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程编号", "课程名称", "教师", "院系", "学生人数", 
                           "考勤次数", "出勤人次", "迟到人次", "旷课人次", "平均出勤率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowIndex = 1;
        for (CourseOffering offering : offerings) {
            List<AttendanceStatistics> statsList = offeringStatsMap.get(offering.getId());
            if (statsList == null || statsList.isEmpty()) {
                continue;
            }

            Row row = sheet.createRow(rowIndex++);
            
            // 计算该课程的统计数据
            int studentCount = statsList.size();
            int totalClasses = statsList.stream()
                    .mapToInt(AttendanceStatistics::getTotalClasses)
                    .max()
                    .orElse(0);
            long present = statsList.stream().mapToInt(AttendanceStatistics::getPresentCount).sum();
            long late = statsList.stream().mapToInt(AttendanceStatistics::getLateCount).sum();
            long absent = statsList.stream().mapToInt(AttendanceStatistics::getAbsentCount).sum();
            
            double avgRate = statsList.stream()
                    .mapToDouble(s -> s.getAttendanceRate() != null ? s.getAttendanceRate() : 0.0)
                    .average()
                    .orElse(0.0);

            int col = 0;
            row.createCell(col++).setCellValue(offering.getCourse().getCourseNo());
            row.createCell(col++).setCellValue(offering.getCourse().getName());
            row.createCell(col++).setCellValue(offering.getTeacher().getName());
            row.createCell(col++).setCellValue(offering.getTeacher().getDepartment().getName());
            row.createCell(col++).setCellValue(studentCount);
            row.createCell(col++).setCellValue(totalClasses);
            row.createCell(col++).setCellValue(present);
            row.createCell(col++).setCellValue(late);
            row.createCell(col++).setCellValue(absent);
            row.createCell(col++).setCellValue(String.format("%.2f", avgRate));

            // 应用样式
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
     * 创建按院系统计表
     */
    private void createDepartmentStatisticsSheet(Workbook workbook, List<CourseOffering> offerings,
                                                Map<Long, List<AttendanceStatistics>> offeringStatsMap,
                                                CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("按院系统计");

        // 按院系分组统计
        Map<String, DepartmentStats> deptStatsMap = new java.util.HashMap<>();
        
        for (CourseOffering offering : offerings) {
            List<AttendanceStatistics> statsList = offeringStatsMap.get(offering.getId());
            if (statsList == null || statsList.isEmpty()) {
                continue;
            }

            String deptName = offering.getTeacher().getDepartment().getName();
            DepartmentStats deptStats = deptStatsMap.computeIfAbsent(deptName, k -> new DepartmentStats());
            
            deptStats.courseCount++;
            deptStats.studentCount += statsList.size();
            
            for (AttendanceStatistics stats : statsList) {
                deptStats.presentCount += stats.getPresentCount();
                deptStats.lateCount += stats.getLateCount();
                deptStats.absentCount += stats.getAbsentCount();
            }
        }

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"院系名称", "开课数", "学生人次", "出勤人次", 
                           "迟到人次", "旷课人次", "平均出勤率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowIndex = 1;
        for (Map.Entry<String, DepartmentStats> entry : deptStatsMap.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            DepartmentStats stats = entry.getValue();
            
            long totalRecords = stats.presentCount + stats.lateCount + stats.absentCount;
            double avgRate = totalRecords > 0 
                    ? (stats.presentCount * 100.0 / totalRecords) 
                    : 0.0;

            int col = 0;
            row.createCell(col++).setCellValue(entry.getKey());
            row.createCell(col++).setCellValue(stats.courseCount);
            row.createCell(col++).setCellValue(stats.studentCount);
            row.createCell(col++).setCellValue(stats.presentCount);
            row.createCell(col++).setCellValue(stats.lateCount);
            row.createCell(col++).setCellValue(stats.absentCount);
            row.createCell(col++).setCellValue(String.format("%.2f", avgRate));

            // 应用样式
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
     * 院系统计数据内部类
     */
    private static class DepartmentStats {
        int courseCount = 0;
        int studentCount = 0;
        long presentCount = 0;
        long lateCount = 0;
        long absentCount = 0;
    }
}

