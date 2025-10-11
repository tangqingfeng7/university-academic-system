package com.university.academic.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成绩公布事件
 * 用于触发学分自动更新
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradePublishEvent {

    /**
     * 成绩ID
     */
    private Long gradeId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程开课ID
     */
    private Long courseOfferingId;

    /**
     * 成绩分数
     */
    private Double score;

    /**
     * 是否及格
     */
    private Boolean passed;

    /**
     * 公布时间
     */
    private LocalDateTime publishedAt;
}

