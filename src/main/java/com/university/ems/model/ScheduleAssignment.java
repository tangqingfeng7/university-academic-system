package com.university.ems.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 排课分配模型
 * 表示一个课程的具体排课结果
 * 
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleAssignment {

    /**
     * 课程开课ID
     */
    private Long courseOfferingId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教室ID
     */
    private Long classroomId;

    /**
     * 教室编号
     */
    private String classroomNo;

    /**
     * 教室容量
     */
    private Integer classroomCapacity;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /**
     * 每周课时数
     */
    private Integer weeklyHours;

    /**
     * 已分配的时间槽列表
     */
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();

    /**
     * 是否已完成排课
     */
    private Boolean scheduled;

    /**
     * 添加时间槽
     */
    public void addTimeSlot(TimeSlot timeSlot) {
        if (timeSlots == null) {
            timeSlots = new ArrayList<>();
        }
        timeSlots.add(timeSlot);
    }

    /**
     * 检查是否已分配足够的时间槽
     */
    public boolean isFullyScheduled() {
        return timeSlots != null && timeSlots.size() >= weeklyHours;
    }
}

