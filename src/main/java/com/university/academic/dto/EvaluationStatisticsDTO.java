package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评价统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationStatisticsDTO {

    /**
     * 开课计划ID
     */
    private Long offeringId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 评价总数
     */
    private Long totalEvaluations;

    /**
     * 学生总数
     */
    private Long totalStudents;

    /**
     * 参与率（%）
     */
    private Double participationRate;

    /**
     * 1星评价数
     */
    private Long count1Star;

    /**
     * 2星评价数
     */
    private Long count2Star;

    /**
     * 3星评价数
     */
    private Long count3Star;

    /**
     * 4星评价数
     */
    private Long count4Star;

    /**
     * 5星评价数
     */
    private Long count5Star;
}

