package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 毕业要求DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraduationRequirementDTO {

    /**
     * 毕业要求ID
     */
    private Long id;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 院系名称
     */
    private String departmentName;

    /**
     * 入学年份
     */
    private Integer enrollmentYear;

    /**
     * 总学分要求
     */
    private Double totalCreditsRequired;

    /**
     * 必修学分要求
     */
    private Double requiredCreditsRequired;

    /**
     * 选修学分要求
     */
    private Double electiveCreditsRequired;

    /**
     * 实践学分要求
     */
    private Double practicalCreditsRequired;

    /**
     * 其他要求（JSON字符串）
     */
    private String additionalRequirements;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

