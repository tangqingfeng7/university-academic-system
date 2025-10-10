package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价周期数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationPeriodDTO {

    /**
     * 评价周期ID
     */
    private Long id;

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否活跃
     */
    private Boolean active;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否在周期内
     */
    private Boolean inPeriod;

    /**
     * 是否已过期
     */
    private Boolean expired;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

