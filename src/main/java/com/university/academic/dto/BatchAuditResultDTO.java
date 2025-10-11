package com.university.academic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量审核结果DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量审核结果信息")
public class BatchAuditResultDTO {

    @Schema(description = "总数", example = "100")
    private Integer totalCount;

    @Schema(description = "成功数", example = "98")
    private Integer successCount;

    @Schema(description = "失败数", example = "2")
    private Integer failCount;

    @Schema(description = "通过数", example = "90")
    private Integer passCount;

    @Schema(description = "不通过数", example = "5")
    private Integer notPassCount;

    @Schema(description = "暂缓毕业数", example = "3")
    private Integer deferredCount;

    @Schema(description = "错误信息列表")
    private List<String> errors;

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 获取通过率
     */
    public double getPassRate() {
        if (successCount == 0) {
            return 0.0;
        }
        return (double) passCount / successCount * 100;
    }
}

