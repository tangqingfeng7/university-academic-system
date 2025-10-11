package com.university.academic.controller;

import com.university.academic.dto.*;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.RefundService;
import com.university.academic.service.TuitionService;
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生学费缴纳Controller
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/tuition")
@RequiredArgsConstructor
@Validated
@Tag(name = "学生学费缴纳管理", description = "学生端学费缴纳相关API")
public class StudentTuitionController {

    private final TuitionService tuitionService;
    private final RefundService refundService;
    private final CustomUserDetailsService userDetailsService;

    // ==================== 学费账单查询 ====================

    @GetMapping("/bills")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的学费账单", description = "查询当前学生的所有学费账单")
    public Result<List<TuitionBillDTO>> getMyBills(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询学费账单", studentId);

        List<TuitionBillDTO> bills = tuitionService.getStudentBills(studentId);
        return Result.success(bills);
    }

    @GetMapping("/bills/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询账单详情", description = "查询指定账单的详细信息，包括缴费记录")
    public Result<TuitionBillDTO> getBillDetail(
            @Parameter(description = "账单ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询账单详情: {}", studentId, id);

        TuitionBillDTO bill = tuitionService.getBillDetail(id);
        
        // 验证账单归属
        if (!bill.getStudentId().equals(studentId)) {
            return Result.error("无权查看该账单");
        }

        return Result.success(bill);
    }

    // ==================== 在线缴费 ====================

    @PostMapping("/payments")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "在线缴费", description = "提交在线缴费订单（支持部分缴费）")
    public Result<TuitionPaymentDTO> createPayment(
            @Parameter(description = "缴费信息") @Valid @RequestBody CreateTuitionPaymentRequest request,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}提交缴费: billId={}, amount={}", studentId, request.getBillId(), request.getAmount());

        TuitionPaymentDTO payment = tuitionService.createPayment(studentId, request);
        return Result.success("缴费成功", payment);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的缴费记录", description = "查询当前学生的所有缴费记录")
    public Result<List<TuitionPaymentDTO>> getMyPayments(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询缴费记录", studentId);

        List<TuitionPaymentDTO> payments = tuitionService.getStudentPayments(studentId);
        return Result.success(payments);
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询缴费详情", description = "查询指定缴费记录的详细信息")
    public Result<TuitionPaymentDTO> getPaymentDetail(
            @Parameter(description = "缴费记录ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询缴费详情: {}", studentId, id);

        TuitionPaymentDTO payment = tuitionService.getPaymentDetail(id);
        
        // 通过账单验证归属
        TuitionBillDTO bill = tuitionService.getBillDetail(payment.getBillId());
        if (!bill.getStudentId().equals(studentId)) {
            return Result.error("无权查看该缴费记录");
        }

        return Result.success(payment);
    }

    // ==================== 电子收据 ====================

    @GetMapping("/receipts/{paymentId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "下载电子收据", description = "下载指定缴费记录的电子收据（PDF格式）")
    public ResponseEntity<byte[]> downloadReceipt(
            @Parameter(description = "缴费记录ID") @PathVariable Long paymentId,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}下载电子收据: paymentId={}", studentId, paymentId);

        // 验证权限
        TuitionPaymentDTO payment = tuitionService.getPaymentDetail(paymentId);
        TuitionBillDTO bill = tuitionService.getBillDetail(payment.getBillId());
        if (!bill.getStudentId().equals(studentId)) {
            return ResponseEntity.badRequest().build();
        }

        byte[] receiptData = tuitionService.generateReceipt(paymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt_" + paymentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(receiptData);
    }

    // ==================== 退费申请 ====================

    @PostMapping(value = "/refund-applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "提交退费申请", description = "学生提交退费申请（支持上传附件）")
    public Result<RefundApplicationDTO> submitRefundApplication(
            @Parameter(description = "退费申请信息") @Valid @ModelAttribute CreateRefundApplicationRequest request,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}提交退费申请: paymentId={}, refundType={}, refundAmount={}", 
                studentId, request.getPaymentId(), request.getRefundType(), request.getRefundAmount());

        RefundApplicationDTO application = refundService.submitRefundApplication(studentId, request);
        return Result.success("退费申请提交成功，等待审批", application);
    }

    @GetMapping("/refund-applications")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的退费申请", description = "查询当前学生的所有退费申请")
    public Result<List<RefundApplicationDTO>> getMyRefundApplications(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询退费申请列表", studentId);

        List<RefundApplicationDTO> applications = refundService.getStudentRefundApplications(studentId);
        return Result.success(applications);
    }

    @GetMapping("/refund-applications/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询退费申请详情", description = "查询指定退费申请的详细信息，包括审批历史")
    public Result<RefundApplicationDTO> getRefundApplicationDetail(
            @Parameter(description = "退费申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询退费申请详情: {}", studentId, id);

        RefundApplicationDTO application = refundService.getRefundApplicationDetail(id);
        
        // 验证归属
        if (!application.getStudentId().equals(studentId)) {
            return Result.error("无权查看该退费申请");
        }

        return Result.success(application);
    }

    @DeleteMapping("/refund-applications/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "取消退费申请", description = "取消待审批的退费申请")
    public Result<Void> cancelRefundApplication(
            @Parameter(description = "退费申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}取消退费申请: {}", studentId, id);

        refundService.cancelRefundApplication(id, studentId);
        return Result.success("退费申请已取消");
    }
}

