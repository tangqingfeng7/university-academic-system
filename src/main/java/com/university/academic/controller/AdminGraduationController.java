package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.dto.*;
import com.university.academic.entity.GraduationAudit;
import com.university.academic.service.GraduateManagementService;
import com.university.academic.service.GraduationAuditService;
import com.university.academic.service.GraduationRequirementService;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 管理员端毕业审核Controller
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/graduation")
@RequiredArgsConstructor
@Tag(name = "管理员端毕业审核管理", description = "毕业要求设置、审核执行、毕业生管理")
public class AdminGraduationController {

    private final GraduationRequirementService graduationRequirementService;
    private final GraduationAuditService graduationAuditService;
    private final GraduateManagementService graduateManagementService;

    // ==================== 毕业要求管理 ====================

    /**
     * 创建毕业要求
     */
    @PostMapping("/requirements")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "创建毕业要求", type = "CREATE")
    @Operation(summary = "创建毕业要求", description = "为指定专业和入学年份设置毕业要求")
    public Result<GraduationRequirementDTO> createRequirement(
            @Valid @RequestBody CreateGraduationRequirementRequest request) {
        log.info("创建毕业要求: majorId={}, enrollmentYear={}", 
                request.getMajorId(), request.getEnrollmentYear());

        GraduationRequirementDTO result = graduationRequirementService.createRequirement(request);
        return Result.success(result);
    }

    /**
     * 更新毕业要求
     */
    @PutMapping("/requirements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "更新毕业要求", type = "UPDATE")
    @Operation(summary = "更新毕业要求", description = "更新指定毕业要求的学分要求")
    public Result<GraduationRequirementDTO> updateRequirement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGraduationRequirementRequest request) {
        log.info("更新毕业要求: id={}", id);

        GraduationRequirementDTO result = graduationRequirementService.updateRequirement(id, request);
        return Result.success(result);
    }

    /**
     * 查询毕业要求
     */
    @GetMapping("/requirements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "查询毕业要求", description = "根据ID查询毕业要求详情")
    public Result<GraduationRequirementDTO> getRequirement(@PathVariable Long id) {
        log.info("查询毕业要求: id={}", id);

        GraduationRequirementDTO result = graduationRequirementService.findById(id);
        return Result.success(result);
    }

    /**
     * 查询专业的毕业要求
     */
    @GetMapping("/requirements")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "查询专业毕业要求", description = "查询指定专业的所有毕业要求")
    public Result<List<GraduationRequirementDTO>> getRequirementsByMajor(
            @RequestParam(required = false) @Parameter(description = "专业ID") Long majorId) {
        log.info("查询专业毕业要求: majorId={}", majorId);

        List<GraduationRequirementDTO> results;
        if (majorId != null) {
            results = graduationRequirementService.findByMajorId(majorId);
        } else {
            results = graduationRequirementService.findAll();
        }
        return Result.success(results);
    }

    /**
     * 删除毕业要求
     */
    @DeleteMapping("/requirements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "删除毕业要求", type = "DELETE")
    @Operation(summary = "删除毕业要求", description = "删除指定的毕业要求")
    public Result<Void> deleteRequirement(@PathVariable Long id) {
        log.info("删除毕业要求: id={}", id);

        graduationRequirementService.deleteRequirement(id);
        return Result.success();
    }

    // ==================== 毕业审核执行 ====================

    /**
     * 执行单个学生审核
     */
    @PostMapping("/audit/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "执行毕业审核", type = "CREATE")
    @Operation(summary = "执行毕业审核", description = "对指定学生执行毕业资格审核")
    public Result<GraduationAuditDTO> performAudit(@PathVariable Long studentId) {
        log.info("执行毕业审核: studentId={}", studentId);

        GraduationAuditDTO result = graduationAuditService.performAudit(studentId);
        return Result.success(result);
    }

    /**
     * 批量审核
     */
    @PostMapping("/audit/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "批量毕业审核", type = "CREATE")
    @Operation(summary = "批量毕业审核", description = "对多个学生批量执行毕业审核")
    public Result<BatchAuditResultDTO> batchPerformAudit(
            @RequestBody @Parameter(description = "学生ID列表") List<Long> studentIds) {
        log.info("批量毕业审核: count={}", studentIds.size());

        BatchAuditResultDTO result = graduationAuditService.batchPerformAudit(studentIds);
        return Result.success(result);
    }

    /**
     * 查询审核记录
     */
    @GetMapping("/audits")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "查询审核记录", description = "查询毕业审核记录列表")
    public Result<List<GraduationAuditDTO>> getAuditRecords(
            @RequestParam(required = false) @Parameter(description = "审核年份") Integer year,
            @RequestParam(required = false) @Parameter(description = "审核状态") GraduationAudit.AuditStatus status) {
        log.info("查询审核记录: year={}, status={}", year, status);

        List<GraduationAuditDTO> results = graduationAuditService.getAuditRecords(year, status);
        return Result.success(results);
    }

    /**
     * 查询学生审核结果
     */
    @GetMapping("/audit/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "查询学生审核结果", description = "查询指定学生的最新审核结果")
    public Result<GraduationAuditDTO> getStudentAuditResult(@PathVariable Long studentId) {
        log.info("查询学生审核结果: studentId={}", studentId);

        GraduationAuditDTO result = graduationAuditService.getAuditResult(studentId);
        return Result.success(result);
    }

    // ==================== 毕业生管理 ====================

    /**
     * 查询毕业生名单
     */
    @GetMapping("/graduates")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "查询毕业生名单", description = "查询指定年份的毕业生名单")
    public Result<List<GraduationAuditDTO>> getGraduateList(
            @RequestParam @Parameter(description = "毕业年份", required = true) Integer year) {
        log.info("查询毕业生名单: year={}", year);

        List<GraduationAuditDTO> results = graduateManagementService.getGraduateList(year);
        return Result.success(results);
    }

    /**
     * 导出毕业生名单
     */
    @GetMapping("/graduates/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @OperationLog(value = "导出毕业生名单", type = "EXPORT")
    @Operation(summary = "导出毕业生名单", description = "导出指定年份的毕业生名单Excel文件")
    public ResponseEntity<byte[]> exportGraduateList(
            @RequestParam @Parameter(description = "毕业年份", required = true) Integer year) {
        log.info("导出毕业生名单: year={}", year);

        try {
            byte[] data = graduateManagementService.exportGraduateList(year);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = URLEncoder.encode(year + "届毕业生名单_" + timestamp + ".xlsx", 
                    StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException e) {
            log.error("导出毕业生名单失败: year={}", year, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 统计毕业生信息
     */
    @GetMapping("/graduates/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_AFFAIRS')")
    @Operation(summary = "统计毕业生信息", description = "统计指定年份的毕业生信息")
    public Result<Map<String, Object>> getGraduateStatistics(
            @RequestParam @Parameter(description = "毕业年份", required = true) Integer year) {
        log.info("统计毕业生信息: year={}", year);

        Map<String, Object> statistics = graduateManagementService.getGraduateStatistics(year);
        return Result.success(statistics);
    }
}

