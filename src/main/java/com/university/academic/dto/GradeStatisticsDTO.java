package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 成绩统计数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeStatisticsDTO {

    /**
     * 总成绩记录数
     */
    private Long totalGrades;

    /**
     * 已发布成绩数
     */
    private Long publishedGrades;

    /**
     * 全局统计
     */
    private GlobalGradeStats globalStats;

    /**
     * 各课程成绩统计
     */
    private List<CourseGradeStats> courseStats;

    /**
     * 全局成绩统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalGradeStats {
        private Double averageScore;      // 平均分
        private Double passRate;          // 及格率（≥60分）
        private Double excellentRate;     // 优秀率（≥90分）
        private Double goodRate;          // 良好率（≥80分）
    }

    /**
     * 课程成绩统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseGradeStats {
        private Long courseOfferingId;
        private String courseName;
        private String teacherName;
        private Long studentCount;        // 学生人数
        private Double averageScore;      // 平均分
        private Double passRate;          // 及格率
        private Double excellentRate;     // 优秀率
        private Double highestScore;      // 最高分
        private Double lowestScore;       // 最低分
    }
}

