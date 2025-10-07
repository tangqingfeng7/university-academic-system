package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 学生统计数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsDTO {

    /**
     * 学生总数
     */
    private Long totalStudents;

    /**
     * 按专业分布（专业名称 -> 学生数量）
     */
    private Map<String, Long> byMajor;

    /**
     * 按年级分布（年级 -> 学生数量）
     */
    private Map<Integer, Long> byGrade;

    /**
     * 按性别分布（性别 -> 学生数量）
     */
    private Map<String, Long> byGender;

    /**
     * 按院系分布（院系名称 -> 学生数量）
     */
    private Map<String, Long> byDepartment;
}

