package com.university.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公示获奖名单请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishAwardsRequest {
    
    /**
     * 奖学金ID
     */
    @NotNull(message = "奖学金ID不能为空")
    private Long scholarshipId;
    
    /**
     * 学年
     */
    @NotBlank(message = "学年不能为空")
    private String academicYear;
}

