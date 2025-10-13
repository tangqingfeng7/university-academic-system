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
        
        // TODO: 实现导出功能（Task 90）
        String fileName = "schedule_" + id + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        byte[] content = new byte[0]; // 暂时返回空内容
        
        HttpHeaders headers = new HttpHeaders();
        if ("pdf".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName + ".pdf");
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName + ".xlsx");
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
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

