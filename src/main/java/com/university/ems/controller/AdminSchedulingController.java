package com.university.ems.controller;

import com.university.academic.vo.Result;
import com.university.ems.dto.*;
import com.university.ems.service.SchedulingConstraintService;
import com.university.ems.service.SchedulingSolutionService;
import com.university.ems.service.TeacherPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理员端排课优化Controller
 * 提供排课约束管理、排课方案管理、智能排课等功能
 */
@RestController
@RequestMapping("/api/admin/scheduling")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
public class AdminSchedulingController {
    
    private final SchedulingConstraintService constraintService;
    private final SchedulingSolutionService solutionService;
    private final TeacherPreferenceService preferenceService;
    
    // ==================== 排课约束管理 ====================
    
    /**
     * 添加排课约束
     */
    @PostMapping("/constraints")
    public Result<SchedulingConstraintDTO> createConstraint(
            @Valid @RequestBody CreateConstraintRequest request) {
        log.info("创建排课约束: {}", request.getName());
        SchedulingConstraintDTO constraint = constraintService.createConstraint(request);
        return Result.success(constraint);
    }
    
    /**
     * 更新排课约束
     */
    @PutMapping("/constraints/{id}")
    public Result<SchedulingConstraintDTO> updateConstraint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConstraintRequest request) {
        log.info("更新排课约束: id={}", id);
        SchedulingConstraintDTO constraint = constraintService.updateConstraint(id, request);
        return Result.success(constraint);
    }
    
    /**
     * 查询排课约束列表
     */
    @GetMapping("/constraints")
    public Result<List<SchedulingConstraintDTO>> getConstraints() {
        log.info("查询排课约束列表");
        List<SchedulingConstraintDTO> constraints = constraintService.getAllConstraints();
        return Result.success(constraints);
    }
    
    /**
     * 查询活跃的约束列表
     */
    @GetMapping("/constraints/active")
    public Result<List<SchedulingConstraintDTO>> getActiveConstraints() {
        log.info("查询活跃的排课约束");
        List<SchedulingConstraintDTO> constraints = constraintService.getActiveConstraints();
        return Result.success(constraints);
    }
    
    /**
     * 查询约束详情
     */
    @GetMapping("/constraints/{id}")
    public Result<SchedulingConstraintDTO> getConstraint(@PathVariable Long id) {
        log.info("查询排课约束详情: id={}", id);
        SchedulingConstraintDTO constraint = constraintService.getConstraintById(id);
        return Result.success(constraint);
    }
    
    /**
     * 删除排课约束
     */
    @DeleteMapping("/constraints/{id}")
    public Result<Void> deleteConstraint(@PathVariable Long id) {
        log.info("删除排课约束: id={}", id);
        constraintService.deleteConstraint(id);
        return Result.success(null);
    }
    
    // ==================== 排课方案管理 ====================
    
    /**
     * 创建排课方案
     */
    @PostMapping("/solutions")
    public Result<SchedulingSolutionDTO> createSolution(
            @Valid @RequestBody CreateSolutionRequest request) {
        log.info("创建排课方案: semesterId={}, name={}", request.getSemesterId(), request.getName());
        SchedulingSolutionDTO solution = solutionService.createSolution(request);
        return Result.success(solution);
    }
    
    /**
     * 执行智能排课
     */
    @PostMapping("/solutions/{id}/optimize")
    public Result<SchedulingResultDTO> executeScheduling(
            @PathVariable Long id,
            @Valid @RequestBody SchedulingRequest request) {
        log.info("执行智能排课: solutionId={}", id);
        SchedulingResultDTO result = solutionService.executeScheduling(id, request);
        return Result.success(result);
    }
    
    /**
     * 手动调整排课
     */
    @PutMapping("/solutions/{id}/adjust")
    public Result<Void> adjustSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleAdjustmentRequest request) {
        log.info("手动调整排课: solutionId={}, courseOfferingId={}", id, request.getCourseOfferingId());
        solutionService.adjustSchedule(id, request);
        return Result.success(null);
    }
    
    /**
     * 应用排课方案
     */
    @PostMapping("/solutions/{id}/apply")
    public Result<Void> applySolution(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApplySolutionRequest request) {
        log.info("应用排课方案: solutionId={}", id);
        if (request == null) {
            request = new ApplySolutionRequest();
        }
        solutionService.applySolution(id, request);
        return Result.success(null);
    }
    
    /**
     * 查询排课方案详情
     */
    @GetMapping("/solutions/{id}")
    public Result<SchedulingSolutionDTO> getSolution(@PathVariable Long id) {
        log.info("查询排课方案详情: id={}", id);
        SchedulingSolutionDTO solution = solutionService.getSolutionById(id);
        return Result.success(solution);
    }
    
    /**
     * 查询排课方案结果（课表详情）
     */
    @GetMapping("/solutions/{id}/result")
    public Result<SchedulingResultDTO> getSolutionResult(@PathVariable Long id) {
        log.info("查询排课方案结果: id={}", id);
        SchedulingResultDTO result = solutionService.getSolutionResult(id);
        return Result.success(result);
    }
    
    /**
     * 检测方案冲突
     */
    @GetMapping("/solutions/{id}/conflicts")
    public Result<List<ConflictDTO>> detectConflicts(@PathVariable Long id) {
        log.info("检测排课方案冲突: id={}", id);
        List<ConflictDTO> conflicts = solutionService.detectConflicts(id);
        return Result.success(conflicts);
    }
    
    /**
     * 查询学期的所有排课方案
     */
    @GetMapping("/solutions")
    public Result<Page<SchedulingSolutionDTO>> getSolutionsBySemester(
            @RequestParam Long semesterId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("查询学期排课方案: semesterId={}", semesterId);
        Page<SchedulingSolutionDTO> solutions = solutionService.getSolutionsBySemester(semesterId, pageable);
        return Result.success(solutions);
    }
    
    /**
     * 删除排课方案
     */
    @DeleteMapping("/solutions/{id}")
    public Result<Void> deleteSolution(@PathVariable Long id) {
        log.info("删除排课方案: id={}", id);
        solutionService.deleteSolution(id);
        return Result.success(null);
    }
    
    /**
     * 比较两个排课方案
     */
    @GetMapping("/solutions/compare")
    public Result<String> compareSolutions(
            @RequestParam Long solutionId1,
            @RequestParam Long solutionId2) {
        log.info("比较排课方案: solutionId1={}, solutionId2={}", solutionId1, solutionId2);
        String comparison = solutionService.compareSolutions(solutionId1, solutionId2);
        return Result.success(comparison);
    }
    
    /**
     * 评估排课方案质量
     */
    @GetMapping("/solutions/{id}/evaluate")
    public Result<Double> evaluateSolution(@PathVariable Long id) {
        log.info("评估排课方案: id={}", id);
        Double score = solutionService.evaluateQuality(id);
        return Result.success(score);
    }
    
    /**
     * 导出课表
     */
    @GetMapping("/solutions/{id}/export")
    public ResponseEntity<byte[]> exportSchedule(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "excel") String format) {
        log.info("导出课表: solutionId={}, format={}", id, format);
        
        try {
            // 获取排课结果
            SchedulingResultDTO result = solutionService.getSolutionResult(id);
            SchedulingSolutionDTO solution = solutionService.getSolutionById(id);
            
            String fileName = "课表_" + solution.getSemesterName() + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            byte[] content;
            HttpHeaders headers = new HttpHeaders();
            
            if ("pdf".equalsIgnoreCase(format)) {
                // PDF导出（未实现）
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", fileName + ".pdf");
                content = new byte[0]; // PDF功能待实现
            } else {
                // Excel导出
                content = generateExcelSchedule(solution, result);
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileName + ".xlsx");
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            log.error("导出课表失败: solutionId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成Excel格式的课表
     */
    private byte[] generateExcelSchedule(SchedulingSolutionDTO solution, SchedulingResultDTO result) 
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建课表工作表
            Sheet sheet = workbook.createSheet("课表");
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // 标题行
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("排课方案：" + solution.getName() + " - " + solution.getSemesterName());
            titleCell.setCellStyle(headerStyle);
            
            // 信息行
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("质量分数：" + 
                (result.getQualityScore() != null ? String.format("%.2f", result.getQualityScore()) : "N/A"));
            
            Row conflictRow = sheet.createRow(rowNum++);
            conflictRow.createCell(0).setCellValue("冲突数：" + result.getConflictCount());
            
            Row countRow = sheet.createRow(rowNum++);
            countRow.createCell(0).setCellValue("已排课程：" + result.getScheduledCourseCount() + 
                " / 总课程：" + (result.getScheduledCourseCount() + result.getUnscheduledCourseCount()));
            
            // 空行
            rowNum++;
            
            // 表头
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"序号", "课程编号", "课程名称", "教师", "星期", "时间段", "教室编号", "教室名称", "学生人数", "质量分"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 数据行
            int index = 1;
            for (ScheduleItemDTO item : result.getScheduleItems()) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(index++);
                dataRow.createCell(1).setCellValue(item.getCourseNo() != null ? item.getCourseNo() : "");
                dataRow.createCell(2).setCellValue(item.getCourseName());
                dataRow.createCell(3).setCellValue(item.getTeacherName());
                dataRow.createCell(4).setCellValue(item.getDayOfWeekDescription());
                dataRow.createCell(5).setCellValue(item.getTimeSlotDescription());
                dataRow.createCell(6).setCellValue(item.getClassroomNo() != null ? item.getClassroomNo() : "");
                dataRow.createCell(7).setCellValue(item.getClassroomName());
                dataRow.createCell(8).setCellValue(item.getStudentCount() != null ? 
                    item.getStudentCount().toString() : "");
                dataRow.createCell(9).setCellValue(item.getSoftConstraintScore() != null ? 
                    String.format("%.2f", item.getSoftConstraintScore()) : "");
                
                // 应用样式
                for (int i = 0; i < headers.length; i++) {
                    dataRow.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(width, 3000));
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
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
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    // ==================== 教师偏好管理（管理员视角） ====================
    
    /**
     * 查询教师的排课偏好
     */
    @GetMapping("/preferences/{teacherId}")
    public Result<TeacherPreferenceDTO> getTeacherPreference(@PathVariable Long teacherId) {
        log.info("管理员查询教师排课偏好: teacherId={}", teacherId);
        TeacherPreferenceDTO preference = preferenceService.getPreferenceByTeacherId(teacherId);
        return Result.success(preference);
    }
    
    /**
     * 查询所有教师的排课偏好
     */
    @GetMapping("/preferences")
    public Result<List<TeacherPreferenceDTO>> getAllPreferences() {
        log.info("查询所有教师排课偏好");
        List<TeacherPreferenceDTO> preferences = preferenceService.getAllPreferences();
        return Result.success(preferences);
    }
    
    /**
     * 管理员代教师设置排课偏好
     */
    @PostMapping("/preferences/{teacherId}")
    public Result<TeacherPreferenceDTO> setTeacherPreference(
            @PathVariable Long teacherId,
            @Valid @RequestBody SetPreferenceRequest request) {
        log.info("管理员为教师设置排课偏好: teacherId={}", teacherId);
        request.setTeacherId(teacherId);
        TeacherPreferenceDTO preference = preferenceService.setPreference(request);
        return Result.success(preference);
    }
}

