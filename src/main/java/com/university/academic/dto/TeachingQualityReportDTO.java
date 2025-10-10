package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教学质量报告DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingQualityReportDTO {

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 课程总数
     */
    private Integer totalCourses;

    /**
     * 教师总数
     */
    private Integer totalTeachers;

    /**
     * 评价总数
     */
    private Long totalEvaluations;

    /**
     * 学生总数
     */
    private Long totalStudents;

    /**
     * 整体参与率（%）
     */
    private Double overallParticipationRate;

    /**
     * 平均课程评分
     */
    private Double averageCourseRating;

    /**
     * 平均教师评分
     */
    private Double averageTeacherRating;

    /**
     * 课程评价统计列表
     */
    private List<EvaluationStatisticsDTO> courseStatistics;

    /**
     * 教师评价统计列表
     */
    private List<TeacherEvaluationStatisticsDTO> teacherStatistics;

    /**
     * 优秀课程列表（评分>=4.5且参与率>=50%）
     */
    private List<EvaluationStatisticsDTO> excellentCourses;

    /**
     * 需改进课程列表（评分<3.0）
     */
    private List<EvaluationStatisticsDTO> coursesNeedImprovement;

    /**
     * 优秀教师列表（综合评分>=4.5）
     */
    private List<TeacherEvaluationStatisticsDTO> excellentTeachers;
}

