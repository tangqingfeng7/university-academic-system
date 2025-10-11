package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.dto.*;
import com.university.academic.entity.ApprovalAction;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.RefundService;
import com.university.academic.service.TuitionService;
import com.university.academic.service.TuitionStatisticsService;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理员学费缴纳Controller
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tuition")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员学费缴纳管理", description = "管理员端学费缴纳相关API")
public class AdminTuitionController {

    private final TuitionService tuitionService;
    private final TuitionStatisticsService statisticsService;
    private final RefundService refundService;
    private final CustomUserDetailsService userDetailsService;

    // ==================== 学费标准管理 ====================

    @PostMapping("/standards")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "设置学费标准", type = "学费管理")
    @Operation(summary = "设置学费标准", description = "为指定专业、学年、年级设置学费标准")
    public Result<TuitionStandardDTO> setStandard(
            @Parameter(description = "学费标准信息") @Valid @RequestBody CreateTuitionStandardRequest request
    ) {
        log.info("管理员设置学费标准: majorId={}, academicYear={}, gradeLevel={}", 
                request.getMajorId(), request.getAcademicYear(), request.getGradeLevel());

        TuitionStandardDTO standard = tuitionService.setStandard(request);
        return Result.success("学费标准设置成功", standard);
    }

    @PutMapping("/standards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "更新学费标准", type = "学费管理")
    @Operation(summary = "更新学费标准", description = "更新指定的学费标准")
    public Result<TuitionStandardDTO> updateStandard(
            @Parameter(description = "学费标准ID") @PathVariable Long id,
            @Parameter(description = "学费标准信息") @Valid @RequestBody CreateTuitionStandardRequest request
    ) {
        log.info("管理员更新学费标准: id={}", id);

        TuitionStandardDTO standard = tuitionService.updateStandard(id, request);
        return Result.success("学费标准更新成功", standard);
    }

    @GetMapping("/standards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询学费标准详情", description = "查询指定学费标准的详细信息")
    public Result<TuitionStandardDTO> getStandard(
            @Parameter(description = "学费标准ID") @PathVariable Long id
    ) {
        TuitionStandardDTO standard = tuitionService.getStandard(id);
        return Result.success(standard);
    }

    @GetMapping("/standards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询学费标准列表", description = "查询指定学年或专业的学费标准，支持分页")
    public Result<Page<TuitionStandardDTO>> getStandards(
            @Parameter(description = "学年，如：2024-2025") @RequestParam(required = false) String academicYear,
            @Parameter(description = "专业ID") @RequestParam(required = false) Long majorId,
            @Parameter(description = "年级") @RequestParam(required = false) Integer gradeLevel,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean active,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        // 将空字符串转换为null，避免查询条件错误
        if (academicYear != null && academicYear.trim().isEmpty()) {
            academicYear = null;
        }
        
        log.info("查询学费标准: academicYear={}, majorId={}, gradeLevel={}, active={}, page={}, size={}", 
                academicYear, majorId, gradeLevel, active, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TuitionStandardDTO> standards = tuitionService.getStandards(
                academicYear, majorId, gradeLevel, active, pageable);

        return Result.success(standards);
    }

    @DeleteMapping("/standards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "停用学费标准", type = "学费管理")
    @Operation(summary = "停用学费标准", description = "停用指定的学费标准")
    public Result<Void> deactivateStandard(
            @Parameter(description = "学费标准ID") @PathVariable Long id
    ) {
        log.info("管理员停用学费标准: id={}", id);

        tuitionService.deactivateStandard(id);
        return Result.success("学费标准已停用");
    }

    // ==================== 学费账单管理 ====================

    @PostMapping("/bills/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "批量生成学费账单", type = "学费管理")
    @Operation(summary = "批量生成学费账单", description = "为所有在校生生成指定学年的学费账单")
    public Result<List<TuitionBillDTO>> batchGenerateBills(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear
    ) {
        log.info("管理员批量生成学费账单: academicYear={}", academicYear);

        List<TuitionBillDTO> bills = tuitionService.batchGenerateBills(academicYear);
        return Result.success("账单生成成功，共生成" + bills.size() + "条", bills);
    }

    @PostMapping("/bills/generate/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "生成单个学生账单", type = "学费管理")
    @Operation(summary = "生成单个学生账单", description = "为指定学生生成学费账单")
    public Result<TuitionBillDTO> generateBill(
            @Parameter(description = "学生ID") @PathVariable Long studentId,
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear
    ) {
        log.info("管理员生成学生账单: studentId={}, academicYear={}", studentId, academicYear);

        TuitionBillDTO bill = tuitionService.generateBill(studentId, academicYear);
        return Result.success("账单生成成功", bill);
    }

    @GetMapping("/bills")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询学费账单", description = "支持多条件筛选查询学费账单")
    public Result<Page<TuitionBillDTO>> searchBills(
            @Parameter(description = "查询条件") @ModelAttribute TuitionBillQueryDTO query,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询学费账单，条件: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<TuitionBillDTO> bills = tuitionService.searchBills(query, pageable);
        return Result.success(bills);
    }

    @GetMapping("/bills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询账单详情", description = "查询指定账单的详细信息，包括缴费记录")
    public Result<TuitionBillDTO> getBillDetail(
            @Parameter(description = "账单ID") @PathVariable Long id
    ) {
        TuitionBillDTO bill = tuitionService.getBillDetail(id);
        return Result.success(bill);
    }

    @GetMapping("/bills/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询逾期账单", description = "查询所有逾期未缴费的账单")
    public Result<List<TuitionBillDTO>> getOverdueBills() {
        log.info("管理员查询逾期账单");

        List<TuitionBillDTO> bills = tuitionService.getOverdueBills();
        return Result.success(bills);
    }

    // ==================== 缴费记录管理 ====================

    @PostMapping("/payments/offline")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "录入线下缴费", type = "学费管理")
    @Operation(summary = "录入线下缴费", description = "录入线下缴费记录（现金、银行转账等）")
    public Result<TuitionPaymentDTO> recordOfflinePayment(
            @Parameter(description = "账单ID") @RequestParam Long billId,
            @Parameter(description = "缴费信息") @Valid @RequestBody RecordPaymentRequest request,
            Authentication authentication
    ) {
        Long operatorId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("管理员录入线下缴费: billId={}, amount={}, operatorId={}", 
                billId, request.getAmount(), operatorId);

        TuitionPaymentDTO payment = tuitionService.recordOfflinePayment(billId, request, operatorId);
        return Result.success("线下缴费录入成功", payment);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询缴费记录", description = "支持多条件筛选查询缴费记录")
    public Result<Page<TuitionPaymentDTO>> searchPayments(
            @Parameter(description = "查询条件") @ModelAttribute PaymentQueryDTO query,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询缴费记录，条件: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<TuitionPaymentDTO> payments = tuitionService.getPayments(query, pageable);
        return Result.success(payments);
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询缴费详情", description = "查询指定缴费记录的详细信息")
    public Result<TuitionPaymentDTO> getPaymentDetail(
            @Parameter(description = "缴费记录ID") @PathVariable Long id
    ) {
        TuitionPaymentDTO payment = tuitionService.getPaymentDetail(id);
        return Result.success(payment);
    }

    // ==================== 欠费提醒 ====================

    @PostMapping("/reminders/send/{billId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "发送欠费提醒", type = "学费管理")
    @Operation(summary = "发送欠费提醒", description = "向指定账单的学生发送欠费提醒")
    public Result<Void> sendReminder(
            @Parameter(description = "账单ID") @PathVariable Long billId
    ) {
        log.info("管理员发送欠费提醒: billId={}", billId);

        tuitionService.sendPaymentReminder(billId);
        return Result.success("欠费提醒已发送");
    }

    @PostMapping("/reminders/batch-send")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "批量发送欠费提醒", type = "学费管理")
    @Operation(summary = "批量发送欠费提醒", description = "向所有逾期未缴费的学生发送欠费提醒")
    public Result<Void> batchSendReminders() {
        log.info("管理员批量发送欠费提醒");

        tuitionService.batchSendPaymentReminders();
        return Result.success("批量欠费提醒已发送");
    }

    // ==================== 财务统计 ====================

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取综合统计数据", description = "获取指定学年的综合统计数据，包含缴费情况和金额统计")
    public Result<PaymentStatisticsDTO> getPaymentStatistics(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear,
            @Parameter(description = "院系ID（可选）") @RequestParam(required = false) Long departmentId
    ) {
        log.info("查询综合统计数据: academicYear={}, departmentId={}", academicYear, departmentId);

        PaymentStatisticsDTO statistics = statisticsService.getPaymentRate(academicYear, departmentId);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/payment-rate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "统计缴费率", description = "统计指定学年的缴费率，可按院系筛选")
    public Result<PaymentStatisticsDTO> getPaymentRate(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear,
            @Parameter(description = "院系ID（可选）") @RequestParam(required = false) Long departmentId
    ) {
        log.info("查询缴费率: academicYear={}, departmentId={}", academicYear, departmentId);

        PaymentStatisticsDTO statistics = statisticsService.getPaymentRate(academicYear, departmentId);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/financial-report")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "生成财务报表", description = "生成指定学年的财务报表，包含收入统计和支付方式分析")
    public Result<FinancialReportDTO> getFinancialReport(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear
    ) {
        log.info("生成财务报表: academicYear={}", academicYear);

        FinancialReportDTO report = statisticsService.generateFinancialReport(academicYear);
        return Result.success(report);
    }

    // ==================== 报表导出 ====================

    @GetMapping("/export/financial-report")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出财务报表", description = "导出指定学年的财务报表（Excel格式）")
    public ResponseEntity<byte[]> exportFinancialReport(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear
    ) {
        log.info("导出财务报表: academicYear={}", academicYear);

        byte[] excelData = statisticsService.exportFinancialReport(academicYear);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String filename = "financial_report_" + academicYear + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/export/bill-list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出账单列表", description = "导出指定学年的账单列表（Excel格式）")
    public ResponseEntity<byte[]> exportBillList(
            @Parameter(description = "学年，如：2024-2025") @RequestParam String academicYear
    ) {
        log.info("导出账单列表: academicYear={}", academicYear);

        byte[] excelData = statisticsService.exportBillList(academicYear);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String filename = "bill_list_" + academicYear + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // ==================== 退费申请管理 ====================

    @GetMapping("/refund-applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询退费申请列表", description = "分页查询退费申请，支持多条件筛选")
    public Result<Page<RefundApplicationDTO>> getRefundApplications(
            @Parameter(description = "学号") @RequestParam(required = false) String studentNo,
            @Parameter(description = "学生姓名") @RequestParam(required = false) String studentName,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询退费申请列表: studentNo={}, studentName={}, page={}, size={}",
                studentNo, studentName, page, size);

        RefundQueryDTO query = RefundQueryDTO.builder()
                .studentNo(studentNo)
                .studentName(studentName)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<RefundApplicationDTO> applications = refundService.searchRefundApplications(query, pageable);

        return Result.success(applications);
    }

    @GetMapping("/refund-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询退费申请详情", description = "查询指定退费申请的详细信息，包括审批历史")
    public Result<RefundApplicationDTO> getRefundApplicationDetail(
            @Parameter(description = "退费申请ID") @PathVariable Long id
    ) {
        log.info("查询退费申请详情: {}", id);

        RefundApplicationDTO application = refundService.getRefundApplicationDetail(id);
        return Result.success(application);
    }

    @PostMapping("/refund-applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "审批退费申请", type = "退费管理")
    @Operation(summary = "审批退费申请", description = "批准或拒绝退费申请")
    public Result<Void> approveRefundApplication(
            @Parameter(description = "退费申请ID") @PathVariable Long id,
            @Parameter(description = "审批信息") @Valid @RequestBody RefundApprovalDTO approvalDTO,
            Authentication authentication
    ) {
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("管理员{}审批退费申请: applicationId={}, action={}", 
                approverId, id, approvalDTO.getAction());

        ApprovalAction action = approvalDTO.getAction();
        refundService.approveRefundApplication(id, approverId, action, approvalDTO.getComment());

        return Result.success(action == ApprovalAction.APPROVE ? "退费申请已批准" : "退费申请已拒绝");
    }

    @PostMapping("/refund-applications/{id}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(value = "执行退费", type = "退费管理")
    @Operation(summary = "执行退费", description = "对已批准的退费申请执行实际退费操作")
    public Result<Void> executeRefund(
            @Parameter(description = "退费申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long operatorId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("管理员{}执行退费: applicationId={}", operatorId, id);

        refundService.executeRefund(id, operatorId);

        return Result.success("退费执行成功");
    }
}

