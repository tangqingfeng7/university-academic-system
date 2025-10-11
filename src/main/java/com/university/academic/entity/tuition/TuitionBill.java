package com.university.academic.entity.tuition;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学费账单实体类
 * 记录学生的学费缴纳账单
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "tuition_bill", indexes = {
    @Index(name = "idx_bill_student", columnList = "student_id"),
    @Index(name = "idx_bill_year", columnList = "academic_year"),
    @Index(name = "idx_bill_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionBill extends BaseEntity {

    /**
     * 学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 学年（如：2024-2025）
     */
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    /**
     * 学费
     */
    @Column(name = "tuition_fee", nullable = false)
    private Double tuitionFee;

    /**
     * 住宿费
     */
    @Column(name = "accommodation_fee", nullable = false)
    @Builder.Default
    private Double accommodationFee = 0.0;

    /**
     * 教材费
     */
    @Column(name = "textbook_fee", nullable = false)
    @Builder.Default
    private Double textbookFee = 0.0;

    /**
     * 其他费用
     */
    @Column(name = "other_fees")
    @Builder.Default
    private Double otherFees = 0.0;

    /**
     * 应缴总额
     */
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    /**
     * 已缴金额
     */
    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private Double paidAmount = 0.0;

    /**
     * 欠费金额
     */
    @Column(name = "outstanding_amount", nullable = false)
    private Double outstandingAmount;

    /**
     * 账单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID;

    /**
     * 缴费截止日期
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * 账单生成时间
     */
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * 更新已缴金额和状态
     */
    public void updatePayment(Double paymentAmount) {
        this.paidAmount += paymentAmount;
        this.outstandingAmount = this.totalAmount - this.paidAmount;
        
        // 更新状态
        if (this.outstandingAmount <= 0) {
            this.status = BillStatus.PAID;
        } else if (this.paidAmount > 0) {
            this.status = BillStatus.PARTIAL;
        }
        
        // 检查是否逾期
        if (this.outstandingAmount > 0 && LocalDate.now().isAfter(this.dueDate)) {
            this.status = BillStatus.OVERDUE;
        }
    }

    /**
     * 检查是否逾期
     */
    public boolean isOverdue() {
        return this.outstandingAmount > 0 && LocalDate.now().isAfter(this.dueDate);
    }
}

