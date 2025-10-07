package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_selection_id", nullable = false, unique = true)
    private CourseSelection courseSelection;

    @Column(name = "regular_score", precision = 5, scale = 2)
    private BigDecimal regularScore;

    @Column(name = "midterm_score", precision = 5, scale = 2)
    private BigDecimal midtermScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GradeStatus status = GradeStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 成绩状态枚举
     */
    public enum GradeStatus {
        DRAFT("草稿"),
        SUBMITTED("已提交"),
        PUBLISHED("已公布");

        private final String description;

        GradeStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算总评成绩
     * 默认：平时30% + 期中30% + 期末40%
     */
    public void calculateTotalScore() {
        if (regularScore != null && midtermScore != null && finalScore != null) {
            this.totalScore = regularScore.multiply(new BigDecimal("0.3"))
                    .add(midtermScore.multiply(new BigDecimal("0.3")))
                    .add(finalScore.multiply(new BigDecimal("0.4")))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 获取绩点（4分制）
     */
    public BigDecimal getGradePoint() {
        if (totalScore == null) {
            return BigDecimal.ZERO;
        }
        
        if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
            return new BigDecimal("4.0");
        } else if (totalScore.compareTo(new BigDecimal("85")) >= 0) {
            return new BigDecimal("3.7");
        } else if (totalScore.compareTo(new BigDecimal("82")) >= 0) {
            return new BigDecimal("3.3");
        } else if (totalScore.compareTo(new BigDecimal("78")) >= 0) {
            return new BigDecimal("3.0");
        } else if (totalScore.compareTo(new BigDecimal("75")) >= 0) {
            return new BigDecimal("2.7");
        } else if (totalScore.compareTo(new BigDecimal("72")) >= 0) {
            return new BigDecimal("2.3");
        } else if (totalScore.compareTo(new BigDecimal("68")) >= 0) {
            return new BigDecimal("2.0");
        } else if (totalScore.compareTo(new BigDecimal("64")) >= 0) {
            return new BigDecimal("1.5");
        } else if (totalScore.compareTo(new BigDecimal("60")) >= 0) {
            return new BigDecimal("1.0");
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 是否及格
     */
    public boolean isPassed() {
        return totalScore != null && totalScore.compareTo(new BigDecimal("60")) >= 0;
    }
}

