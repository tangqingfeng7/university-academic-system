package com.university.academic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 毕业进度DTO
 * 包含学分完成情况和审核状态
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "毕业进度信息")
public class GraduationProgressDTO {

    @Schema(description = "学分汇总信息")
    private StudentCreditSummaryDTO credits;

    @Schema(description = "毕业审核信息")
    private GraduationAuditDTO audit;

    @Schema(description = "毕业要求信息")
    private GraduationRequirementDTO requirement;

    /**
     * 获取毕业进度百分比
     * 基于总学分完成情况
     */
    @Schema(description = "毕业进度百分比", example = "85.5")
    public Double getProgressPercentage() {
        if (credits == null || requirement == null) {
            return 0.0;
        }
        double totalCredits = credits.getTotalCredits();
        double requiredTotal = requirement.getTotalCreditsRequired();
        if (requiredTotal == 0) {
            return 0.0;
        }
        return Math.min(100.0, (totalCredits / requiredTotal) * 100);
    }

    /**
     * 检查是否可以申请毕业审核
     */
    @Schema(description = "是否可以申请毕业审核")
    public Boolean isEligibleForAudit() {
        if (credits == null || requirement == null) {
            return false;
        }
        // 总学分达到90%以上可以申请审核
        return credits.getTotalCredits() >= requirement.getTotalCreditsRequired() * 0.9;
    }

    /**
     * 获取还需学分数
     */
    @Schema(description = "还需总学分", example = "15.0")
    public Double getRemainingCredits() {
        if (credits == null || requirement == null) {
            return 0.0;
        }
        double remaining = requirement.getTotalCreditsRequired() - credits.getTotalCredits();
        return Math.max(0.0, remaining);
    }

    /**
     * 获取必修学分差距
     */
    @Schema(description = "还需必修学分", example = "5.0")
    public Double getRemainingRequiredCredits() {
        if (credits == null || requirement == null) {
            return 0.0;
        }
        double remaining = requirement.getRequiredCreditsRequired() - credits.getRequiredCredits();
        return Math.max(0.0, remaining);
    }

    /**
     * 获取选修学分差距
     */
    @Schema(description = "还需选修学分", example = "8.0")
    public Double getRemainingElectiveCredits() {
        if (credits == null || requirement == null) {
            return 0.0;
        }
        double remaining = requirement.getElectiveCreditsRequired() - credits.getElectiveCredits();
        return Math.max(0.0, remaining);
    }

    /**
     * 获取实践学分差距
     */
    @Schema(description = "还需实践学分", example = "2.0")
    public Double getRemainingPracticalCredits() {
        if (credits == null || requirement == null) {
            return 0.0;
        }
        double remaining = requirement.getPracticalCreditsRequired() - credits.getPracticalCredits();
        return Math.max(0.0, remaining);
    }

    /**
     * 检查是否已通过毕业审核
     */
    @Schema(description = "是否已通过毕业审核")
    public Boolean isPassed() {
        return audit != null && audit.isPassed();
    }
}

