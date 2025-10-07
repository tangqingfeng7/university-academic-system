package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 课表数据传输对象
 * 包含完整的课表信息
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {

    /**
     * 学期ID
     */
    private Long semesterId;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 课表项列表
     */
    private List<ScheduleItemDTO> items;

    /**
     * 按周次分组的课表
     * key: 周次（1-20），value: 该周的课程列表
     */
    private Map<Integer, List<ScheduleItemDTO>> weeklySchedule;

    /**
     * 按星期和节次分组的课表（用于网格展示）
     * key: "day-period"（如"1-1-2"表示周一第1-2节），value: 课程信息
     */
    private Map<String, ScheduleItemDTO> gridSchedule;
}

