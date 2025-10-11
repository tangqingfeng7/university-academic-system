package com.university.academic.service.impl;

import com.university.academic.dto.AwardStatisticsDTO;
import com.university.academic.dto.PublishAwardsRequest;
import com.university.academic.dto.ScholarshipAwardDTO;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ScholarshipApplicationRepository;
import com.university.academic.repository.ScholarshipAwardRepository;
import com.university.academic.repository.ScholarshipRepository;
import com.university.academic.service.ScholarshipAwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 奖学金获奖记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipAwardServiceImpl implements ScholarshipAwardService {
    
    private final ScholarshipAwardRepository awardRepository;
    private final ScholarshipApplicationRepository applicationRepository;
    private final ScholarshipRepository scholarshipRepository;
    
    /**
     * 公示获奖名单
     */
    @Override
    @Transactional
    public Integer publishAwards(PublishAwardsRequest request) {
        log.info("公示获奖名单: scholarshipId={}, academicYear={}", 
                request.getScholarshipId(), request.getAcademicYear());
        
        // 1. 验证奖学金存在
        Scholarship scholarship = scholarshipRepository.findById(request.getScholarshipId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 2. 查询所有院系已批准的申请
        List<ScholarshipApplication> approvedApplications = applicationRepository
                .findByScholarshipIdAndAcademicYear(
                        request.getScholarshipId(), 
                        request.getAcademicYear()
                ).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.DEPT_APPROVED)
                .collect(Collectors.toList());
        
        if (approvedApplications.isEmpty()) {
            log.warn("没有找到已批准的申请: scholarshipId={}, academicYear={}", 
                    request.getScholarshipId(), request.getAcademicYear());
            return 0;
        }
        
        // 3. 创建获奖记录
        List<ScholarshipAward> awards = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (ScholarshipApplication application : approvedApplications) {
            // 检查是否已经创建过获奖记录
            if (awardRepository.existsByStudentIdAndScholarshipIdAndAcademicYear(
                    application.getStudent().getId(), 
                    request.getScholarshipId(), 
                    request.getAcademicYear())) {
                log.debug("学生已有获奖记录，跳过: studentId={}", application.getStudent().getId());
                continue;
            }
            
            ScholarshipAward award = new ScholarshipAward();
            award.setApplication(application);
            award.setStudent(application.getStudent());
            award.setScholarship(scholarship);
            award.setAcademicYear(request.getAcademicYear());
            award.setAmount(scholarship.getAmount());
            award.setAwardedAt(now);
            award.setPublished(true);
            
            awards.add(award);
        }
        
        // 4. 批量保存
        if (!awards.isEmpty()) {
            awardRepository.saveAll(awards);
            log.info("获奖名单已公示: count={}", awards.size());
        }
        
        return awards.size();
    }
    
    /**
     * 查询学生的获奖记录
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipAwardDTO> getStudentAwards(Long studentId) {
        log.debug("查询学生获奖记录: studentId={}", studentId);
        
        List<ScholarshipAward> awards = awardRepository.findByStudentIdWithScholarship(studentId);
        
        return awards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询获奖名单（分页）
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ScholarshipAwardDTO> getAwards(
            Long scholarshipId, String academicYear, Boolean published, Pageable pageable) {
        
        log.debug("查询获奖名单: scholarshipId={}, academicYear={}, published={}", 
                scholarshipId, academicYear, published);
        
        Page<ScholarshipAward> awardPage;
        
        if (scholarshipId != null && academicYear != null) {
            // 按奖学金和学年查询
            awardPage = awardRepository.findByScholarshipIdAndAcademicYear(
                    scholarshipId, academicYear, pageable);
        } else if (academicYear != null && Boolean.TRUE.equals(published)) {
            // 查询已公示的
            awardPage = awardRepository.findPublishedByAcademicYear(academicYear, pageable);
        } else if (academicYear != null) {
            // 按学年查询（转换为Page）
            List<ScholarshipAward> awards = awardRepository.findByAcademicYear(academicYear);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), awards.size());
            List<ScholarshipAward> pageContent = awards.subList(start, end);
            awardPage = new PageImpl<>(pageContent, pageable, awards.size());
        } else {
            // 查询所有（带关联数据）
            awardPage = awardRepository.findAllWithDetails(pageable);
        }
        
        return awardPage.map(this::convertToDTO);
    }
    
    /**
     * 导出获奖名单（Excel）
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportAwardList(Long scholarshipId, String academicYear) {
        log.info("导出获奖名单: scholarshipId={}, academicYear={}", scholarshipId, academicYear);
        
        // 查询获奖记录
        List<ScholarshipAward> awards;
        if (scholarshipId != null) {
            awards = awardRepository.findByScholarshipIdAndAcademicYear(
                    scholarshipId, academicYear, Pageable.unpaged()).getContent();
        } else {
            awards = awardRepository.findByAcademicYear(academicYear);
        }
        
        if (awards.isEmpty()) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_AWARD_NOT_FOUND);
        }
        
        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("获奖名单");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "学号", "姓名", "院系", "专业", "年级", 
                              "奖学金名称", "奖学金等级", "金额", "GPA", "综合得分", "获奖时间"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            CellStyle dataStyle = createDataStyle(workbook);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            int rowNum = 1;
            for (ScholarshipAward award : awards) {
                Row row = sheet.createRow(rowNum++);
                
                Student student = award.getStudent();
                Major major = student.getMajor();
                Department department = major.getDepartment();
                Scholarship scholarship = award.getScholarship();
                ScholarshipApplication application = award.getApplication();
                
                int colNum = 0;
                createCell(row, colNum++, rowNum - 1, dataStyle);
                createCell(row, colNum++, student.getStudentNo(), dataStyle);
                createCell(row, colNum++, student.getName(), dataStyle);
                createCell(row, colNum++, department.getName(), dataStyle);
                createCell(row, colNum++, major.getName(), dataStyle);
                createCell(row, colNum++, calculateGrade(student), dataStyle);
                createCell(row, colNum++, scholarship.getName(), dataStyle);
                createCell(row, colNum++, scholarship.getLevel().getDescription(), dataStyle);
                createCell(row, colNum++, award.getAmount(), dataStyle);
                createCell(row, colNum++, application != null ? application.getGpa() : 0.0, dataStyle);
                createCell(row, colNum++, application != null ? application.getComprehensiveScore() : 0.0, dataStyle);
                createCell(row, colNum++, award.getAwardedAt().format(formatter), dataStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            log.info("获奖名单导出成功: count={}", awards.size());
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("导出获奖名单失败", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED);
        }
    }
    
    /**
     * 获奖统计
     */
    @Override
    @Transactional(readOnly = true)
    public AwardStatisticsDTO getStatistics(String academicYear) {
        log.debug("获奖统计: academicYear={}", academicYear);
        
        List<ScholarshipAward> awards = awardRepository.findByAcademicYear(academicYear);
        
        if (awards.isEmpty()) {
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
     * 转换为DTO
     */
    private ScholarshipAwardDTO convertToDTO(ScholarshipAward award) {
        Student student = award.getStudent();
        Major major = student.getMajor();
        Department department = major.getDepartment();
        Scholarship scholarship = award.getScholarship();
        ScholarshipApplication application = award.getApplication();
        
        return ScholarshipAwardDTO.builder()
                .id(award.getId())
                .applicationId(application != null ? application.getId() : null)
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .departmentName(department.getName())
                .majorName(major.getName())
                .grade(calculateGrade(student))
                .scholarshipId(scholarship.getId())
                .scholarshipName(scholarship.getName())
                .scholarshipLevel(scholarship.getLevel().getDescription())
                .academicYear(award.getAcademicYear())
                .amount(award.getAmount())
                .gpa(application != null ? application.getGpa() : null)
                .comprehensiveScore(application != null ? application.getComprehensiveScore() : null)
                .awardedAt(award.getAwardedAt())
                .published(award.getPublished())
                .createdAt(award.getCreatedAt())
                .build();
    }
    
    /**
     * 计算学生年级
     */
    private Integer calculateGrade(Student student) {
        int currentYear = java.time.Year.now().getValue();
        return currentYear - student.getEnrollmentYear() + 1;
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

