package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课表项数据传输对象
 * 表示课表中的一个课程项
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDTO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师姓名（学生视角）
     */
    private String teacherName;

    /**
     * 学生信息（教师视角）- 显示选课学生数
     */
    private Integer studentCount;

    /**
     * 上课地点
     */
    private String location;

    /**
     * 星期几（1-7，1表示周一）
     */
    private Integer day;

    /**
     * 节次（如"1-2"表示第1-2节）
     */
    private String period;

    /**
     * 周次列表（如[1,2,3,4,5]表示第1-5周）
     */
    private List<Integer> weeks;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 开课计划ID
     */
    private Long offeringId;
}

