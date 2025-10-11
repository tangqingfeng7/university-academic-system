package com.university.academic.controller;

import com.university.academic.dto.*;
import com.university.academic.service.*;
import com.university.academic.vo.Result;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 管理员端奖学金Controller
 * 提供奖学金管理、自动评定、获奖记录管理、统计分析等功能
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
public class AdminScholarshipController {
    
    private final ScholarshipService scholarshipService;
    private final ScholarshipApplicationService applicationService;
    private final ScholarshipAwardService awardService;
    private final ScholarshipStatisticsService statisticsService;
    
    // ==================== 奖学金管理 ====================
    
    /**
     * 创建奖学金
     */
    @PostMapping("/scholarships")
    public Result<ScholarshipDTO> createScholarship(@Valid @RequestBody CreateScholarshipRequest request) {
        log.info("创建奖学金: {}", request.getName());
        ScholarshipDTO scholarship = scholarshipService.createScholarship(request);
        return Result.success(scholarship);
    }
    
    /**
     * 更新奖学金
     */
    @PutMapping("/scholarships/{id}")
    public Result<ScholarshipDTO> updateScholarship(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScholarshipRequest request) {
        log.info("更新奖学金: id={}", id);
        ScholarshipDTO scholarship = scholarshipService.updateScholarship(id, request);
        return Result.success(scholarship);
    }
    
    /**
     * 查询奖学金列表
     */
    @GetMapping("/scholarships")
    public Result<Page<ScholarshipDTO>> getScholarships(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("查询奖学金列表");
        Page<ScholarshipDTO> scholarships = scholarshipService.getScholarships(pageable);
        return Result.success(scholarships);
    }
    
    /**
     * 查询奖学金详情
     */
    @GetMapping("/scholarships/{id}")
    public Result<ScholarshipDTO> getScholarship(@PathVariable Long id) {
        log.info("查询奖学金: id={}", id);
        ScholarshipDTO scholarship = scholarshipService.getScholarshipById(id);
        return Result.success(scholarship);
    }
    
    /**
     * 启用/禁用奖学金
     */
    @PutMapping("/scholarships/{id}/active")
    public Result<Void> toggleActive(@PathVariable Long id, @RequestParam Boolean active) {
        log.info("切换奖学金状态: id={}, active={}", id, active);
        scholarshipService.toggleActive(id, active);
        return Result.success();
    }
    
    /**
     * 删除奖学金
     */
    @DeleteMapping("/scholarships/{id}")
    public Result<Void> deleteScholarship(@PathVariable Long id) {
        log.info("删除奖学金: id={}", id);
        scholarshipService.deleteScholarship(id);
        return Result.success();
    }
    
    /**
     * 开启申请
     */
    @PostMapping("/scholarships/{id}/open")
    public Result<Void> openApplication(
            @PathVariable Long id,
            @Valid @RequestBody OpenApplicationRequest request) {
        log.info("开启奖学金申请: id={}, academicYear={}", id, request.getAcademicYear());
        scholarshipService.openApplication(id, request);
        return Result.success();
    }
    
    // ==================== 申请管理 ====================
    
    /**
     * 查询申请列表
     */
    @GetMapping("/scholarship-applications")
    public Result<Page<ScholarshipApplicationDTO>> getApplications(
            ApplicationQueryDTO query,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("查询申请列表: {}", query);
        Page<ScholarshipApplicationDTO> applications = applicationService.getApplications(query, pageable);
        return Result.success(applications);
    }
    
    /**
     * 查询申请详情
     */
    @GetMapping("/scholarship-applications/{id}")
    public Result<ScholarshipApplicationDTO> getApplication(@PathVariable Long id) {
        log.info("查询申请详情: id={}", id);
        ScholarshipApplicationDTO application = applicationService.getApplicationById(id);
        return Result.success(application);
    }
    
    // ==================== 自动评定 ====================
    
    /**
     * 获取默认评定规则
     */
    @GetMapping("/scholarships/evaluation-rules/default")
    public Result<EvaluationRuleDTO> getDefaultRule() {
        log.info("获取默认评定规则");
        // 需要注入ScholarshipEvaluationService
        return Result.success(null);
    }
    
    /**
     * 解析奖学金评定规则
     */
    @GetMapping("/scholarships/{id}/evaluation-rule")
    public Result<EvaluationRuleDTO> getScholarshipRule(@PathVariable Long id) {
        log.info("解析奖学金评定规则: id={}", id);
        // 需要注入ScholarshipEvaluationService
        return Result.success(null);
    }
    
    /**
     * 自动评定奖学金
     */
    @PostMapping("/scholarships/evaluate")
    public Result<EvaluationResultDTO> autoEvaluate(@Valid @RequestBody EvaluationRequest request) {
        log.info("自动评定奖学金: scholarshipId={}, academicYear={}", 
                request.getScholarshipId(), request.getAcademicYear());
        EvaluationResultDTO result = applicationService.autoEvaluate(request);
        return Result.success(result);
    }
    
    // ==================== 获奖记录管理 ====================
    
    /**
     * 公示获奖名单
     */
    @PostMapping("/scholarship-awards/publish")
    public Result<Integer> publishAwards(@Valid @RequestBody PublishAwardsRequest request) {
        log.info("公示获奖名单: scholarshipId={}, academicYear={}", 
                request.getScholarshipId(), request.getAcademicYear());
        Integer count = awardService.publishAwards(request);
        return Result.success(count);
    }
    
    /**
     * 查询获奖名单
     */
    @GetMapping("/scholarship-awards")
    public Result<Page<ScholarshipAwardDTO>> getAwards(
            @RequestParam(required = false) Long scholarshipId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Boolean published,
            @PageableDefault(size = 20, sort = "awardedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("查询获奖名单: scholarshipId={}, academicYear={}, published={}", 
                scholarshipId, academicYear, published);
        Page<ScholarshipAwardDTO> awards = awardService.getAwards(scholarshipId, academicYear, published, pageable);
        return Result.success(awards);
    }
    
    /**
     * 导出获奖名单
     */
    @GetMapping("/scholarship-awards/export")
    public ResponseEntity<byte[]> exportAwards(
            @RequestParam(required = false) Long scholarshipId,
            @RequestParam String academicYear) {
        log.info("导出获奖名单: scholarshipId={}, academicYear={}", scholarshipId, academicYear);
        
        byte[] excelData = awardService.exportAwardList(scholarshipId, academicYear);
        
        String filename = String.format("获奖名单_%s_%s.xlsx", 
                academicYear, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(excelData.length);
        
        return ResponseEntity.ok().headers(headers).body(excelData);
    }
    
    // ==================== 统计分析 ====================
    
    /**
     * 获奖分布统计
     */
    @GetMapping("/scholarship-statistics/distribution")
    public Result<AwardStatisticsDTO> getDistribution(@RequestParam String academicYear) {
        log.info("获奖分布统计: academicYear={}", academicYear);
        AwardStatisticsDTO statistics = statisticsService.getAwardDistribution(academicYear);
        return Result.success(statistics);
    }
    
    /**
     * 按专业统计
     */
    @GetMapping("/scholarship-statistics/by-major")
    public Result<Map<String, Integer>> getByMajor(
            @RequestParam String academicYear,
            @RequestParam(required = false) Long departmentId) {
        log.info("按专业统计: academicYear={}, departmentId={}", academicYear, departmentId);
        Map<String, Integer> statistics = statisticsService.getStatisticsByMajor(academicYear, departmentId);
        return Result.success(statistics);
    }
    
    /**
     * 按年级统计
     */
    @GetMapping("/scholarship-statistics/by-grade")
    public Result<Map<Integer, Integer>> getByGrade(@RequestParam String academicYear) {
        log.info("按年级统计: academicYear={}", academicYear);
        Map<Integer, Integer> statistics = statisticsService.getStatisticsByGrade(academicYear);
        return Result.success(statistics);
    }
    
    /**
     * 生成统计报告
     */
    @GetMapping("/scholarship-statistics/report")
    public ResponseEntity<byte[]> generateReport(@RequestParam String academicYear) {
        log.info("生成统计报告: academicYear={}", academicYear);
        
        byte[] reportData = statisticsService.generateStatisticsReport(academicYear);
        
        String filename = String.format("奖学金统计报告_%s_%s.xlsx", 
                academicYear, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(reportData.length);
        
        return ResponseEntity.ok().headers(headers).body(reportData);
    }
    
    /**
     * 年度对比统计
     */
    @GetMapping("/scholarship-statistics/yearly-comparison")
    public Result<List<AwardStatisticsDTO>> getYearlyComparison(
            @RequestParam String startYear,
            @RequestParam String endYear) {
        log.info("年度对比统计: startYear={}, endYear={}", startYear, endYear);
        List<AwardStatisticsDTO> comparison = statisticsService.getYearlyComparison(startYear, endYear);
        return Result.success(comparison);
    }
}

