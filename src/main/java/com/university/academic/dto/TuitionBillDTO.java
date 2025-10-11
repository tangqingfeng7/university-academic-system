package com.university.academic.dto;

import com.university.academic.entity.tuition.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学费账单DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionBillDTO {

    /**
     * 账单ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 院系名称
     */
    private String departmentName;

    /**
     * 年级
     */
    private Integer enrollmentYear;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 学费
     */
    private Double tuitionFee;

    /**
     * 住宿费
     */
    private Double accommodationFee;

    /**
     * 教材费
     */
    private Double textbookFee;

    /**
     * 其他费用
     */
    private Double otherFees;

    /**
     * 应缴总额
     */
    private Double totalAmount;

    /**
     * 已缴金额
     */
    private Double paidAmount;

    /**
     * 欠费金额
     */
    private Double outstandingAmount;

    /**
     * 账单状态
     */
    private BillStatus status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 缴费截止日期
     */
    private LocalDate dueDate;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 账单生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 缴费记录
     */
    private List<TuitionPaymentDTO> payments;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

