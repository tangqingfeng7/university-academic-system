package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师工作量统计数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherWorkloadDTO {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 所属院系
     */
    private String departmentName;

    /**
     * 职称
     */
    private String title;

    /**
     * 授课门数
     */
    private Long courseCount;

    /**
     * 授课班级数
     */
    private Long offeringCount;

    /**
     * 授课学生总人数
     */
    private Long totalStudents;

    /**
     * 总学时
     */
    private Long totalHours;

    /**
     * 平均每班人数
     */
    private Double averageStudentsPerClass;
}

