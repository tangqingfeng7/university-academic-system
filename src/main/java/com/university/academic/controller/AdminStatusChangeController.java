package com.university.academic.controller;

import com.university.academic.vo.Result;
import com.university.academic.dto.StatusChangeQueryDTO;
import com.university.academic.dto.StatusChangeStatisticsDTO;
import com.university.academic.dto.StudentStatusChangeDTO;
import com.university.academic.dto.ApprovalRequest;
import com.university.academic.dto.converter.StatusChangeConverter;
import com.university.academic.entity.StudentStatusChange;
import com.university.academic.repository.StudentStatusChangeRepository;
import com.university.academic.service.StatusChangeStatisticsService;
import com.university.academic.service.StatusChangeApprovalService;
import com.university.academic.security.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 管理员学籍异动Controller
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/status-changes")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员学籍异动管理", description = "管理员端学籍异动相关API")
public class AdminStatusChangeController {

    private final StatusChangeStatisticsService statisticsService;
    private final StatusChangeConverter statusChangeConverter;
    private final StudentStatusChangeRepository statusChangeRepository;
    private final StatusChangeApprovalService approvalService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询所有异动申请", description = "支持多条件筛选查询异动申请")
    public Result<Page<StudentStatusChangeDTO>> queryApplications(
            @Parameter(description = "查询条件") @ModelAttribute StatusChangeQueryDTO queryDTO,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询异动申请，条件: {}", queryDTO);

        Pageable pageable = PageRequest.of(page, size);
        Page<StudentStatusChange> statusChangePage = statisticsService.queryStatusChanges(queryDTO, pageable);

        Page<StudentStatusChangeDTO> dtoPage = statusChangePage.map(sc -> statusChangeConverter.toDTO(sc, false));
        return Result.success(dtoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询申请详情", description = "查询指定申请的详细信息，包含审批历史")
    public Result<StudentStatusChangeDTO> getApplicationDetail(
            @Parameter(description = "异动申请ID") @PathVariable Long id
    ) {
        log.info("管理员查询申请详情: {}", id);

        // 使用 Repository 的 findByIdWithDetails 方法查询（已预加载关联对象）
        StudentStatusChange statusChange = statusChangeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, true);
        return Result.success(dto);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审批异动申请", description = "管理员审批异动申请（批准、拒绝或退回）")
    public Result<StudentStatusChangeDTO> approveApplication(
            @Parameter(description = "异动申请ID") @PathVariable Long id,
            @Parameter(description = "审批信息") @Valid @RequestBody ApprovalRequest request,
            Authentication authentication
    ) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("管理员{}审批申请{}: {}", approverId, id, request.getAction());

        StudentStatusChange statusChange = approvalService.approveApplication(
                id,
                approverId,
                request.getAction(),
                request.getComment()
        );

        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, true);
        return Result.success("审批成功", dto);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询超时申请", description = "查询所有超时未处理的异动申请")
    public Result<Page<StudentStatusChangeDTO>> getOverdueApplications(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询超时申请");

        Pageable pageable = PageRequest.of(page, size);
        Page<StudentStatusChange> overduePage = statisticsService.getOverdueApplications(pageable);

        Page<StudentStatusChangeDTO> dtoPage = overduePage.map(sc -> statusChangeConverter.toDTO(sc, false));
        return Result.success(dtoPage);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询异动统计", description = "获取指定时间段内的异动统计数据")
    public Result<StatusChangeStatisticsDTO> getStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员查询异动统计，时间段: {} - {}", startDate, endDate);

        StatusChangeStatisticsDTO statistics = statisticsService.getStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/by-type")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "按类型统计", description = "按异动类型统计数量")
    public Result<StatusChangeStatisticsDTO> getStatisticsByType(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员按类型统计异动，时间段: {} - {}", startDate, endDate);

        StatusChangeStatisticsDTO statistics = statisticsService.getStatisticsByType(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "按状态统计", description = "按审批状态统计数量")
    public Result<StatusChangeStatisticsDTO> getStatisticsByStatus(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员按状态统计异动，时间段: {} - {}", startDate, endDate);

        StatusChangeStatisticsDTO statistics = statisticsService.getStatisticsByStatus(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/by-month")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "按月份统计", description = "按月份统计异动趋势")
    public Result<StatusChangeStatisticsDTO> getStatisticsByMonth(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员按月份统计异动，时间段: {} - {}", startDate, endDate);

        StatusChangeStatisticsDTO statistics = statisticsService.getStatisticsByMonth(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "转专业流向统计", description = "统计转专业的目标专业分布")
    public Result<StatusChangeStatisticsDTO> getTransferStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员查询转专业流向统计，时间段: {} - {}", startDate, endDate);

        StatusChangeStatisticsDTO statistics = statisticsService.getTransferStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出异动记录", description = "导出符合条件的异动记录为Excel文件")
    public ResponseEntity<byte[]> exportStatusChanges(
            @Parameter(description = "查询条件") @ModelAttribute StatusChangeQueryDTO queryDTO
    ) {
        log.info("管理员导出异动记录，条件: {}", queryDTO);

        byte[] excelData = statisticsService.exportStatusChanges(queryDTO);

        String filename = "学籍异动记录_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @GetMapping("/export/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出统计报告", description = "导出指定时间段的统计报告为Excel文件")
    public ResponseEntity<byte[]> exportStatisticsReport(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("管理员导出统计报告，时间段: {} - {}", startDate, endDate);

        byte[] excelData = statisticsService.exportStatisticsReport(startDate, endDate);

        String filename = "学籍异动统计报告_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}

