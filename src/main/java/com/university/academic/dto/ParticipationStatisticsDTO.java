package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参与率统计DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationStatisticsDTO {

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 学生总数
     */
    private Long totalStudents;

    /**
     * 已评价人数
     */
    private Long totalEvaluated;

    /**
     * 参与率（%）
     */
    private Double participationRate;

    /**
     * 高参与率课程数（>=80%）
     */
    private Integer highParticipationCourses;

    /**
     * 中参与率课程数（50-80%）
     */
    private Integer mediumParticipationCourses;

    /**
     * 低参与率课程数（<50%）
     */
    private Integer lowParticipationCourses;
}

