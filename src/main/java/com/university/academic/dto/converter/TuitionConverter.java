package com.university.academic.dto.converter;

import com.university.academic.dto.*;
import com.university.academic.entity.Student;
import com.university.academic.entity.tuition.*;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学费缴纳DTO转换器
 *
 * @author Academic System Team
 */
@Component
@RequiredArgsConstructor
public class TuitionConverter {

    private final UserRepository userRepository;

    /**
     * 将学费标准实体转换为DTO
     */
    public TuitionStandardDTO toTuitionStandardDTO(TuitionStandard entity) {
        if (entity == null) {
            return null;
        }

        // Null安全检查
        if (entity.getMajor() == null) {
            throw new IllegalStateException("学费标准关联的专业信息为空: standardId=" + entity.getId());
        }

        return TuitionStandardDTO.builder()
                .id(entity.getId())
                .majorId(entity.getMajor().getId())
                .majorName(entity.getMajor().getName())
                .departmentName(entity.getMajor().getDepartment() != null 
                    ? entity.getMajor().getDepartment().getName() : "未知院系")
                .academicYear(entity.getAcademicYear())
                .gradeLevel(entity.getGradeLevel())
                .tuitionFee(entity.getTuitionFee())
                .accommodationFee(entity.getAccommodationFee())
                .textbookFee(entity.getTextbookFee())
                .otherFees(entity.getOtherFees())
                .totalFee(entity.getTotalFee())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 将学费账单实体转换为DTO
     */
    public TuitionBillDTO toTuitionBillDTO(TuitionBill entity) {
        return toTuitionBillDTO(entity, false);
    }

    /**
     * 将学费账单实体转换为DTO
     *
     * @param entity          账单实体
     * @param includePayments 是否包含缴费记录
     */
    public TuitionBillDTO toTuitionBillDTO(TuitionBill entity, boolean includePayments) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        
        // Null安全检查
        if (student == null) {
            throw new IllegalStateException("账单关联的学生信息为空: billId=" + entity.getId());
        }

        TuitionBillDTO.TuitionBillDTOBuilder builder = TuitionBillDTO.builder()
                .id(entity.getId())
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .majorName(student.getMajor() != null ? student.getMajor().getName() : "未知专业")
                .departmentName(student.getMajor() != null && student.getMajor().getDepartment() != null 
                    ? student.getMajor().getDepartment().getName() : "未知院系")
                .enrollmentYear(student.getEnrollmentYear())
                .academicYear(entity.getAcademicYear())
                .tuitionFee(entity.getTuitionFee())
                .accommodationFee(entity.getAccommodationFee())
                .textbookFee(entity.getTextbookFee())
                .otherFees(entity.getOtherFees())
                .totalAmount(entity.getTotalAmount())
                .paidAmount(entity.getPaidAmount())
                .outstandingAmount(entity.getOutstandingAmount())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .dueDate(entity.getDueDate())
                .isOverdue(entity.isOverdue())
                .generatedAt(entity.getGeneratedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 如果需要包含缴费记录，这里可以添加
        if (includePayments) {
            builder.payments(Collections.emptyList()); // 需要从其他地方查询
        }

        return builder.build();
    }

    /**
     * 将缴费记录实体转换为DTO
     */
    public TuitionPaymentDTO toTuitionPaymentDTO(TuitionPayment entity) {
        if (entity == null) {
            return null;
        }

        TuitionBill bill = entity.getBill();
        
        // Null安全检查
        if (bill == null) {
            throw new IllegalStateException("缴费记录关联的账单信息为空: paymentId=" + entity.getId());
        }
        
        Student student = bill.getStudent();
        if (student == null) {
            throw new IllegalStateException("账单关联的学生信息为空: billId=" + bill.getId());
        }

        TuitionPaymentDTO.TuitionPaymentDTOBuilder builder = TuitionPaymentDTO.builder()
                .id(entity.getId())
                .billId(bill.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .academicYear(bill.getAcademicYear())
                .paymentNo(entity.getPaymentNo())
                .amount(entity.getAmount())
                .method(entity.getMethod())
                .methodDescription(entity.getMethod().getDescription())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .transactionId(entity.getTransactionId())
                .paidAt(entity.getPaidAt())
                .receiptUrl(entity.getReceiptUrl())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 如果有操作员ID，获取操作员姓名
        if (entity.getOperatorId() != null) {
            builder.operatorId(entity.getOperatorId());
            userRepository.findById(entity.getOperatorId())
                    .ifPresent(user -> builder.operatorName(user.getUsername()));
        }

        return builder.build();
    }

    /**
     * 将缴费提醒实体转换为DTO
     */
    public PaymentReminderDTO toPaymentReminderDTO(PaymentReminder entity) {
        if (entity == null) {
            return null;
        }

        TuitionBill bill = entity.getBill();
        Student student = bill.getStudent();

        return PaymentReminderDTO.builder()
                .id(entity.getId())
                .billId(bill.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .reminderType(entity.getReminderType())
                .sentAt(entity.getSentAt())
                .success(entity.getSuccess())
                .failureReason(entity.getFailureReason())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 批量转换学费标准
     */
    public List<TuitionStandardDTO> toTuitionStandardDTOList(List<TuitionStandard> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTuitionStandardDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换学费账单
     */
    public List<TuitionBillDTO> toTuitionBillDTOList(List<TuitionBill> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTuitionBillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换缴费记录
     */
    public List<TuitionPaymentDTO> toTuitionPaymentDTOList(List<TuitionPayment> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTuitionPaymentDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换缴费提醒
     */
    public List<PaymentReminderDTO> toPaymentReminderDTOList(List<PaymentReminder> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toPaymentReminderDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将退费申请实体转换为DTO
     */
    public RefundApplicationDTO toRefundApplicationDTO(RefundApplication entity) {
        return toRefundApplicationDTO(entity, false);
    }

    /**
     * 将退费申请实体转换为DTO
     *
     * @param entity              退费申请实体
     * @param includeApprovalHistory 是否包含审批历史
     */
    public RefundApplicationDTO toRefundApplicationDTO(RefundApplication entity, 
                                                       boolean includeApprovalHistory) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        TuitionPayment payment = entity.getPayment();

        RefundApplicationDTO.RefundApplicationDTOBuilder builder = RefundApplicationDTO.builder()
                .id(entity.getId())
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .paymentId(payment.getId())
                .paymentNo(payment.getPaymentNo())
                .paymentAmount(payment.getAmount())
                .refundAmount(entity.getRefundAmount())
                .reason(entity.getReason())
                .refundType(entity.getRefundType())
                .refundTypeDescription(entity.getRefundType().getDescription())
                .refundMethod(entity.getRefundMethod())
                .refundMethodDescription(entity.getRefundMethod().getDescription())
                .bankAccount(entity.getBankAccount())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .approvalLevel(entity.getApprovalLevel())
                .submittedAt(entity.getSubmittedAt())
                .approvedAt(entity.getApprovedAt())
                .refundedAt(entity.getRefundedAt())
                .refundTransactionId(entity.getRefundTransactionId())
                .attachmentUrl(entity.getAttachmentUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (includeApprovalHistory) {
            builder.approvalHistory(Collections.emptyList()); // 需要从其他地方查询
        }

        return builder.build();
    }

    /**
     * 将退费审批记录实体转换为DTO
     */
    public RefundApprovalDTO toRefundApprovalDTO(RefundApproval entity) {
        if (entity == null) {
            return null;
        }

        String levelDescription;
        switch (entity.getApprovalLevel()) {
            case 1 -> levelDescription = "财务审核";
            case 2 -> levelDescription = "管理员审批";
            default -> levelDescription = "未知级别";
        }

        return RefundApprovalDTO.builder()
                .id(entity.getId())
                .approvalLevel(entity.getApprovalLevel())
                .approvalLevelDescription(levelDescription)
                .approverId(entity.getApprover().getId())
                .approverName(entity.getApprover().getUsername())
                .action(entity.getAction())
                .actionDescription(entity.getAction().getDescription())
                .comment(entity.getComment())
                .approvedAt(entity.getApprovedAt())
                .build();
    }

    /**
     * 批量转换退费申请
     */
    public List<RefundApplicationDTO> toRefundApplicationDTOList(List<RefundApplication> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toRefundApplicationDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换退费审批记录
     */
    public List<RefundApprovalDTO> toRefundApprovalDTOList(List<RefundApproval> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toRefundApprovalDTO)
                .collect(Collectors.toList());
    }
}

