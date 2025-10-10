package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师评价统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEvaluationStatisticsDTO {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 平均教学能力评分
     */
    private Double averageTeachingRating;

    /**
     * 平均教学态度评分
     */
    private Double averageAttitudeRating;

    /**
     * 平均教学内容评分
     */
    private Double averageContentRating;

    /**
     * 综合评分
     */
    private Double overallRating;

    /**
     * 评价总数
     */
    private Long totalEvaluations;
}

