package com.university.academic.controller;

import com.university.academic.vo.Result;
import com.university.academic.dto.ApprovalRequest;
import com.university.academic.dto.StudentStatusChangeDTO;
import com.university.academic.dto.converter.StatusChangeConverter;
import com.university.academic.entity.StudentStatusChange;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.StatusChangeApprovalService;
import com.university.academic.service.StatusChangeStatisticsService;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审批人学籍异动Controller
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/approver/status-changes")
@RequiredArgsConstructor
@Validated
@Tag(name = "审批人学籍异动管理", description = "审批人端学籍异动相关API")
public class ApproverStatusChangeController {

    private final StatusChangeApprovalService approvalService;
    private final StatusChangeStatisticsService statisticsService;
    private final StatusChangeConverter statusChangeConverter;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询待审批申请", description = "查询当前审批人的待审批申请列表")
    public Result<Page<StudentStatusChangeDTO>> getPendingApplications(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("审批人{}查询待审批申请", approverId);

        Pageable pageable = PageRequest.of(page, size);
        Page<StudentStatusChange> pendingPage = statisticsService.getPendingApplications(approverId, pageable);

        Page<StudentStatusChangeDTO> dtoPage = pendingPage.map(sc -> statusChangeConverter.toDTO(sc, false));
        return Result.success(dtoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询申请详情", description = "查询指定申请的详细信息，包含审批历史")
    public Result<StudentStatusChangeDTO> getApplicationDetail(
            @Parameter(description = "异动申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("审批人{}查询申请详情: {}", approverId, id);

        StudentStatusChange statusChange = approvalService.getApplicationForApproval(id, approverId);
        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, true);

        return Result.success(dto);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "审批异动申请", description = "批准、拒绝或退回异动申请")
    public Result<StudentStatusChangeDTO> approveApplication(
            @Parameter(description = "异动申请ID") @PathVariable Long id,
            @Parameter(description = "审批信息") @Valid @RequestBody ApprovalRequest request,
            Authentication authentication
    ) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("审批人{}审批申请{}: {}", approverId, id, request.getAction());

        StudentStatusChange statusChange = approvalService.approveApplication(
                id,
                approverId,
                request.getAction(),
                request.getComment()
        );

        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, true);
        return Result.success("审批成功", dto);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询审批统计", description = "查询当前审批人的审批统计信息")
    public Result<ApproverStatisticsDTO> getApprovalStatistics(Authentication authentication) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("审批人{}查询审批统计", approverId);

        // 查询待审批数量
        Page<StudentStatusChange> pendingPage = statisticsService.getPendingApplications(
                approverId, PageRequest.of(0, 1));
        long pendingCount = pendingPage.getTotalElements();

        ApproverStatisticsDTO statistics = ApproverStatisticsDTO.builder()
                .approverId(approverId)
                .pendingCount(pendingCount)
                .build();

        return Result.success(statistics);
    }

    /**
     * 审批人统计DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApproverStatisticsDTO {
        private Long approverId;
        private Long pendingCount;
    }
}

