package com.university.academic.dto;

import com.university.academic.entity.GraduationAudit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 毕业审核DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "毕业审核信息")
public class GraduationAuditDTO {

    @Schema(description = "审核ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "审核年份", example = "2024")
    private Integer auditYear;

    @Schema(description = "总学分", example = "150.0")
    private Double totalCredits;

    @Schema(description = "必修学分", example = "100.0")
    private Double requiredCredits;

    @Schema(description = "选修学分", example = "40.0")
    private Double electiveCredits;

    @Schema(description = "实践学分", example = "10.0")
    private Double practicalCredits;

    @Schema(description = "审核状态")
    private GraduationAudit.AuditStatus status;

    @Schema(description = "审核状态描述")
    private String statusDescription;

    @Schema(description = "不通过原因")
    private String failReason;

    @Schema(description = "审核人ID")
    private Long auditedBy;

    @Schema(description = "审核人姓名")
    private String auditedByName;

    @Schema(description = "审核时间")
    private LocalDateTime auditedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 是否通过审核
     */
    public boolean isPassed() {
        return status == GraduationAudit.AuditStatus.PASS;
    }

    /**
     * 是否暂缓毕业
     */
    public boolean isDeferred() {
        return status == GraduationAudit.AuditStatus.DEFERRED;
    }
}

