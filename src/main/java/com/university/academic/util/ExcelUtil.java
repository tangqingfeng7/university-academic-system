package com.university.academic.util;

import com.university.academic.dto.StudentDTO;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel工具类
 * 提供Excel导入导出功能
 *
 * @author Academic System Team
 */
@Slf4j
@Component
public class ExcelUtil {

    /**
     * 导入学生数据（从Excel文件）
     *
     * @param inputStream Excel文件输入流
     * @return 学生信息列表
     */
    public List<StudentImportDTO> importStudents(InputStream inputStream) {
        List<StudentImportDTO> students = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            int rowNum = 0;
            for (Row row : sheet) {
                if (rowNum++ == 0) {
                    continue; // 跳过标题行
                }
                
                try {
                    StudentImportDTO student = new StudentImportDTO();
                    student.setRowNumber(rowNum);
                    
                    // 学号（必填）
                    Cell studentNoCell = row.getCell(0);
                    if (studentNoCell == null || getCellValue(studentNoCell).trim().isEmpty()) {
                        student.setErrorMessage("学号不能为空");
                        students.add(student);
                        continue;
                    }
                    student.setStudentNo(getCellValue(studentNoCell).trim());
                    
                    // 姓名（必填）
                    Cell nameCell = row.getCell(1);
                    if (nameCell == null || getCellValue(nameCell).trim().isEmpty()) {
                        student.setErrorMessage("姓名不能为空");
                        students.add(student);
                        continue;
                    }
                    student.setName(getCellValue(nameCell).trim());
                    
                    // 性别（必填）
                    Cell genderCell = row.getCell(2);
                    if (genderCell == null || getCellValue(genderCell).trim().isEmpty()) {
                        student.setErrorMessage("性别不能为空");
                        students.add(student);
                        continue;
                    }
                    String genderStr = getCellValue(genderCell).trim();
                    if ("男".equals(genderStr)) {
                        student.setGender("MALE");
                    } else if ("女".equals(genderStr)) {
                        student.setGender("FEMALE");
                    } else {
                        student.setErrorMessage("性别格式错误（应为：男/女）");
                        students.add(student);
                        continue;
                    }
                    
                    // 出生日期
                    Cell birthDateCell = row.getCell(3);
                    if (birthDateCell != null && !getCellValue(birthDateCell).trim().isEmpty()) {
                        if (birthDateCell.getCellType() == CellType.NUMERIC) {
                            Date date = birthDateCell.getDateCellValue();
                            student.setBirthDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                        }
                    }
                    
                    // 入学年份（必填）
                    Cell enrollmentYearCell = row.getCell(4);
                    if (enrollmentYearCell == null || getCellValue(enrollmentYearCell).trim().isEmpty()) {
                        student.setErrorMessage("入学年份不能为空");
                        students.add(student);
                        continue;
                    }
                    try {
                        student.setEnrollmentYear(Integer.parseInt(getCellValue(enrollmentYearCell).trim()));
                    } catch (NumberFormatException e) {
                        student.setErrorMessage("入学年份格式错误");
                        students.add(student);
                        continue;
                    }
                    
                    // 专业代码（必填）
                    Cell majorCodeCell = row.getCell(5);
                    if (majorCodeCell == null || getCellValue(majorCodeCell).trim().isEmpty()) {
                        student.setErrorMessage("专业代码不能为空");
                        students.add(student);
                        continue;
                    }
                    student.setMajorCode(getCellValue(majorCodeCell).trim());
                    
                    // 班级
                    Cell classNameCell = row.getCell(6);
                    if (classNameCell != null) {
                        student.setClassName(getCellValue(classNameCell).trim());
                    }
                    
                    // 联系电话
                    Cell phoneCell = row.getCell(7);
                    if (phoneCell != null) {
                        student.setPhone(getCellValue(phoneCell).trim());
                    }
                    
                    student.setValid(true);
                    students.add(student);
                } catch (Exception e) {
                    log.error("解析第{}行数据时出错", rowNum, e);
                    StudentImportDTO errorStudent = new StudentImportDTO();
                    errorStudent.setRowNumber(rowNum);
                    errorStudent.setErrorMessage("数据解析失败: " + e.getMessage());
                    students.add(errorStudent);
                }
            }
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(ErrorCode.STUDENT_IMPORT_ERROR);
        }
        
        return students;
    }

    /**
     * 导出学生数据（到Excel文件）
     *
     * @param students 学生列表
     * @return Excel文件字节数组
     */
    public byte[] exportStudents(List<StudentDTO> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("学生信息");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "性别", "出生日期", "入学年份", "专业", "班级", "联系电话", "用户名"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // 填充数据
            int rowNum = 1;
            for (StudentDTO student : students) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, student.getStudentNo(), dataStyle);
                createCell(row, 1, student.getName(), dataStyle);
                createCell(row, 2, "MALE".equals(student.getGender()) ? "男" : "女", dataStyle);
                createCell(row, 3, student.getBirthDate() != null ? student.getBirthDate().toString() : "", dataStyle);
                createCell(row, 4, student.getEnrollmentYear() != null ? student.getEnrollmentYear().toString() : "", dataStyle);
                createCell(row, 5, student.getMajorName() != null ? student.getMajorName() : "", dataStyle);
                createCell(row, 6, student.getClassName() != null ? student.getClassName() : "", dataStyle);
                createCell(row, 7, student.getPhone() != null ? student.getPhone() : "", dataStyle);
                createCell(row, 8, student.getUsername() != null ? student.getUsername() : "", dataStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512); // 额外增加一点宽度
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("导出Excel文件失败", e);
            throw new BusinessException(ErrorCode.STUDENT_EXPORT_ERROR);
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 创建单元格并设置值和样式
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * 导入成绩数据（从Excel文件）
     *
     * @param inputStream Excel文件输入流
     * @return 成绩信息列表
     */
    public List<GradeImportDTO> importGrades(InputStream inputStream) {
        List<GradeImportDTO> grades = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            int rowNum = 0;
            for (Row row : sheet) {
                if (rowNum++ == 0) {
                    continue; // 跳过标题行
                }
                
                try {
                    GradeImportDTO grade = new GradeImportDTO();
                    grade.setRowNumber(rowNum);
                    
                    // 学号（必填）
                    Cell studentNoCell = row.getCell(0);
                    if (studentNoCell == null || getCellValue(studentNoCell).trim().isEmpty()) {
                        grade.setErrorMessage("学号不能为空");
                        grades.add(grade);
                        continue;
                    }
                    grade.setStudentNo(getCellValue(studentNoCell).trim());
                    
                    // 姓名（可选，用于验证）
                    Cell nameCell = row.getCell(1);
                    if (nameCell != null) {
                        grade.setStudentName(getCellValue(nameCell).trim());
                    }
                    
                    // 平时成绩
                    Cell regularScoreCell = row.getCell(2);
                    if (regularScoreCell != null && !getCellValue(regularScoreCell).trim().isEmpty()) {
                        try {
                            double score = Double.parseDouble(getCellValue(regularScoreCell).trim());
                            if (score < 0 || score > 100) {
                                grade.setErrorMessage("平时成绩必须在0-100之间");
                                grades.add(grade);
                                continue;
                            }
                            grade.setRegularScore(java.math.BigDecimal.valueOf(score));
                        } catch (NumberFormatException e) {
                            grade.setErrorMessage("平时成绩格式错误");
                            grades.add(grade);
                            continue;
                        }
                    }
                    
                    // 期中成绩
                    Cell midtermScoreCell = row.getCell(3);
                    if (midtermScoreCell != null && !getCellValue(midtermScoreCell).trim().isEmpty()) {
                        try {
                            double score = Double.parseDouble(getCellValue(midtermScoreCell).trim());
                            if (score < 0 || score > 100) {
                                grade.setErrorMessage("期中成绩必须在0-100之间");
                                grades.add(grade);
                                continue;
                            }
                            grade.setMidtermScore(java.math.BigDecimal.valueOf(score));
                        } catch (NumberFormatException e) {
                            grade.setErrorMessage("期中成绩格式错误");
                            grades.add(grade);
                            continue;
                        }
                    }
                    
                    // 期末成绩
                    Cell finalScoreCell = row.getCell(4);
                    if (finalScoreCell != null && !getCellValue(finalScoreCell).trim().isEmpty()) {
                        try {
                            double score = Double.parseDouble(getCellValue(finalScoreCell).trim());
                            if (score < 0 || score > 100) {
                                grade.setErrorMessage("期末成绩必须在0-100之间");
                                grades.add(grade);
                                continue;
                            }
                            grade.setFinalScore(java.math.BigDecimal.valueOf(score));
                        } catch (NumberFormatException e) {
                            grade.setErrorMessage("期末成绩格式错误");
                            grades.add(grade);
                            continue;
                        }
                    }
                    
                    grade.setValid(true);
                    grades.add(grade);
                } catch (Exception e) {
                    log.error("解析第{}行数据时出错", rowNum, e);
                    GradeImportDTO errorGrade = new GradeImportDTO();
                    errorGrade.setRowNumber(rowNum);
                    errorGrade.setErrorMessage("数据解析失败: " + e.getMessage());
                    grades.add(errorGrade);
                }
            }
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(ErrorCode.GRADE_IMPORT_ERROR);
        }
        
        return grades;
    }

    /**
     * 学生导入DTO
     */
    public static class StudentImportDTO {
        private int rowNumber;
        private String studentNo;
        private String name;
        private String gender;
        private LocalDate birthDate;
        private Integer enrollmentYear;
        private String majorCode;
        private String className;
        private String phone;
        private boolean valid;
        private String errorMessage;

        // Getters and Setters
        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

        public Integer getEnrollmentYear() { return enrollmentYear; }
        public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }

        public String getMajorCode() { return majorCode; }
        public void setMajorCode(String majorCode) { this.majorCode = majorCode; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 成绩导入DTO
     */
    public static class GradeImportDTO {
        private int rowNumber;
        private String studentNo;
        private String studentName;
        private java.math.BigDecimal regularScore;
        private java.math.BigDecimal midtermScore;
        private java.math.BigDecimal finalScore;
        private boolean valid;
        private String errorMessage;

        // Getters and Setters
        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public java.math.BigDecimal getRegularScore() { return regularScore; }
        public void setRegularScore(java.math.BigDecimal regularScore) { this.regularScore = regularScore; }

        public java.math.BigDecimal getMidtermScore() { return midtermScore; }
        public void setMidtermScore(java.math.BigDecimal midtermScore) { this.midtermScore = midtermScore; }

        public java.math.BigDecimal getFinalScore() { return finalScore; }
        public void setFinalScore(java.math.BigDecimal finalScore) { this.finalScore = finalScore; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 导出统计报表（Excel）
     *
     * @param title      报表标题
     * @param headers    表头
     * @param dataList   数据列表（每行数据）
     * @return Excel文件字节数组
     */
    public byte[] exportStatisticsReport(String title, String[] headers, List<Object[]> dataList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(title);

            // 创建样式
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;

            // 创建标题行
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            // 合并标题单元格
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                    0, 0, 0, headers.length - 1));

            // 创建表头
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            for (Object[] rowData : dataList) {
                Row dataRow = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = dataRow.createCell(i);
                    Object value = rowData[i];
                    
                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 增加一点额外宽度
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出统计报表失败", e);
            throw new BusinessException(ErrorCode.EXPORT_ERROR);
        }
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
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}

