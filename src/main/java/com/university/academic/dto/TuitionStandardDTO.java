package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学费标准DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionStandardDTO {

    /**
     * 学费标准ID
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
     * 学年
     */
    private String academicYear;

    /**
     * 年级
     */
    private Integer gradeLevel;

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
     * 总费用
     */
    private Double totalFee;

    /**
     * 是否启用
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

