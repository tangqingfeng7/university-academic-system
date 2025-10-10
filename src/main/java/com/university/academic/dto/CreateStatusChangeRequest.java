package com.university.academic.dto;

import com.university.academic.entity.ChangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 创建学籍异动申请请求DTO
 *
 * @author university
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStatusChangeRequest {

    /**
     * 异动类型
     */
    @NotNull(message = "异动类型不能为空")
    private ChangeType type;

    /**
     * 异动原因
     */
    @NotBlank(message = "异动原因不能为空")
    private String reason;

    /**
     * 开始日期（休学、复学时必填）
     */
    private LocalDate startDate;

    /**
     * 结束日期（休学时必填）
     */
    private LocalDate endDate;

    /**
     * 目标专业ID（转专业时必填）
     */
    private Long targetMajorId;

    /**
     * 证明材料附件
     */
    private MultipartFile attachment;
}

