package com.university.academic.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.university.academic.dto.GradeDTO;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * PDF工具类
 * 提供PDF生成功能，主要用于成绩单导出
 *
 * @author Academic System Team
 */
@Slf4j
@Component
public class PdfUtil {

    // 字体定义
    private static BaseFont baseFont;
    private static Font titleFont;
    private static Font headerFont;
    private static Font contentFont;
    private static Font boldFont;

    static {
        try {
            // 使用iText内置的中文字体
            baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            titleFont = new Font(baseFont, 18, Font.BOLD);
            headerFont = new Font(baseFont, 14, Font.BOLD);
            contentFont = new Font(baseFont, 10, Font.NORMAL);
            boldFont = new Font(baseFont, 10, Font.BOLD);
        } catch (Exception e) {
            log.error("初始化PDF字体失败", e);
        }
    }

    /**
     * 生成学生成绩单PDF
     *
     * @param studentInfo 学生基本信息
     * @param grades 成绩列表
     * @param statistics 统计信息
     * @return PDF文件字节数组
     */
    public byte[] generateTranscriptPdf(Map<String, Object> studentInfo,
                                       List<GradeDTO> grades,
                                       Map<String, Object> statistics) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 添加标题
            addTitle(document, "学生成绩单");
            document.add(new Paragraph("\n"));
            
            // 添加学生基本信息
            addStudentInfo(document, studentInfo);
            document.add(new Paragraph("\n"));
            
            // 添加成绩表格
            addGradesTable(document, grades);
            document.add(new Paragraph("\n"));
            
            // 添加统计信息
            addStatistics(document, statistics);
            
            // 添加页脚
            addFooter(document);
            
            document.close();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("生成成绩单PDF失败", e);
            throw new BusinessException(ErrorCode.GRADE_EXPORT_ERROR);
        }
    }

    /**
     * 添加标题
     */
    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);
    }

    /**
     * 添加学生基本信息
     */
    private void addStudentInfo(Document document, Map<String, Object> studentInfo) 
            throws DocumentException {
        PdfPTable infoTable = new PdfPTable(4); // 4列
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);
        
        // 设置边框样式
        PdfPCell cell;
        
        // 第一行：学号和姓名
        cell = createInfoCell("学号：" + studentInfo.get("studentNo"));
        infoTable.addCell(cell);
        
        cell = createInfoCell("姓名：" + studentInfo.get("name"));
        infoTable.addCell(cell);
        
        cell = createInfoCell("性别：" + studentInfo.get("gender"));
        infoTable.addCell(cell);
        
        cell = createInfoCell("入学年份：" + studentInfo.get("enrollmentYear"));
        infoTable.addCell(cell);
        
        // 第二行：专业和班级
        cell = createInfoCell("专业：" + studentInfo.get("majorName"));
        cell.setColspan(2);
        infoTable.addCell(cell);
        
        cell = createInfoCell("班级：" + studentInfo.getOrDefault("className", ""));
        cell.setColspan(2);
        infoTable.addCell(cell);
        
        document.add(infoTable);
    }

    /**
     * 创建信息单元格
     */
    private PdfPCell createInfoCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(5);
        return cell;
    }

    /**
     * 添加成绩表格
     */
    private void addGradesTable(Document document, List<GradeDTO> grades) 
            throws DocumentException {
        // 添加成绩明细标题
        Paragraph gradeTitle = new Paragraph("成绩明细", headerFont);
        document.add(gradeTitle);
        document.add(new Paragraph("\n", contentFont));
        
        // 创建表格：序号、课程编号、课程名称、学分、学期、平时、期中、期末、总评、绩点
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        
        // 设置列宽
        float[] columnWidths = {0.5f, 1f, 2f, 0.6f, 1f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f};
        table.setWidths(columnWidths);
        
        // 添加表头
        addTableHeader(table, new String[]{
            "序号", "课程编号", "课程名称", "学分", "学期", 
            "平时", "期中", "期末", "总评", "绩点"
        });
        
        // 添加数据行
        int index = 1;
        for (GradeDTO grade : grades) {
            addTableCell(table, String.valueOf(index++), Element.ALIGN_CENTER);
            addTableCell(table, grade.getCourseNo(), Element.ALIGN_CENTER);
            addTableCell(table, grade.getCourseName(), Element.ALIGN_LEFT);
            addTableCell(table, String.valueOf(grade.getCredits()), Element.ALIGN_CENTER);
            addTableCell(table, grade.getSemesterName(), Element.ALIGN_CENTER);
            addTableCell(table, formatScore(grade.getRegularScore()), Element.ALIGN_CENTER);
            addTableCell(table, formatScore(grade.getMidtermScore()), Element.ALIGN_CENTER);
            addTableCell(table, formatScore(grade.getFinalScore()), Element.ALIGN_CENTER);
            addTableCell(table, formatScore(grade.getTotalScore()), Element.ALIGN_CENTER);
            addTableCell(table, formatScore(grade.getGradePoint()), Element.ALIGN_CENTER);
        }
        
        document.add(table);
    }

    /**
     * 添加表头
     */
    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    /**
     * 添加表格单元格
     */
    private void addTableCell(PdfPTable table, String content, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * 格式化分数
     */
    private String formatScore(BigDecimal score) {
        if (score == null) {
            return "-";
        }
        return score.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * 添加统计信息
     */
    private void addStatistics(Document document, Map<String, Object> statistics) 
            throws DocumentException {
        // 添加统计标题
        Paragraph statsTitle = new Paragraph("成绩统计", headerFont);
        document.add(statsTitle);
        document.add(new Paragraph("\n", contentFont));
        
        // 创建统计信息表格
        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(50);
        statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // 添加统计数据
        addStatRow(statsTable, "总课程数", statistics.get("totalCourses") + "门");
        addStatRow(statsTable, "已通过课程", statistics.get("passedCourses") + "门");
        addStatRow(statsTable, "总学分", statistics.get("totalCredits") + "分");
        addStatRow(statsTable, "已获学分", statistics.get("passedCredits") + "分");
        
        BigDecimal gpa = (BigDecimal) statistics.get("overallGPA");
        String gpaStr = gpa != null ? gpa.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
        addStatRow(statsTable, "平均绩点(GPA)", gpaStr);
        
        document.add(statsTable);
    }

    /**
     * 添加统计行
     */
    private void addStatRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + "：", boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, contentFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    /**
     * 添加页脚
     */
    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));
        
        Paragraph footer = new Paragraph(
            "打印时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date()),
            contentFont
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
        
        Paragraph note = new Paragraph("注：本成绩单由教务系统自动生成", contentFont);
        note.setAlignment(Element.ALIGN_CENTER);
        document.add(note);
    }
}

