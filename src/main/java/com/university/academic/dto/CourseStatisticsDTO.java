package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程统计数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsDTO {

    /**
     * 课程总数
     */
    private Long totalCourses;

    /**
     * 开课总数
     */
    private Long totalOfferings;

    /**
     * 选课总人次
     */
    private Long totalSelections;

    /**
     * 平均容量利用率（百分比）
     */
    private Double averageCapacityUtilization;

    /**
     * 各课程详细统计
     */
    private List<CourseOfferingStatDTO> courseDetails;

    /**
     * 课程开课统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseOfferingStatDTO {
        private Long offeringId;
        private String courseName;
        private String teacherName;
        private Integer capacity;
        private Integer enrolled;
        private Double utilizationRate; // 容量利用率
    }
}

