package com.university.academic.entity.tuition;

import com.university.academic.entity.BaseEntity;
import com.university.academic.entity.Major;
import jakarta.persistence.*;
import lombok.*;

/**
 * 学费标准实体类
 * 记录不同专业、年级的学费标准
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "tuition_standard", indexes = {
    @Index(name = "idx_standard_major", columnList = "major_id"),
    @Index(name = "idx_standard_year", columnList = "academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionStandard extends BaseEntity {

    /**
     * 专业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    /**
     * 学年（如：2024-2025）
     */
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    /**
     * 年级
     */
    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

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
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 计算总费用
     */
    public Double getTotalFee() {
        return tuitionFee + accommodationFee + textbookFee + (otherFees != null ? otherFees : 0.0);
    }
}

