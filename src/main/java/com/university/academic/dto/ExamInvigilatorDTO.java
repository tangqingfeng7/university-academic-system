package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监考安排数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamInvigilatorDTO {

    /**
     * 监考安排ID
     */
    private Long id;

    /**
     * 考场ID
     */
    private Long examRoomId;

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
     * 院系名称
     */
    private String departmentName;

    /**
     * 监考类型
     */
    private String type;

    /**
     * 监考类型描述
     */
    private String typeDescription;

    /**
     * 完整教师信息（用于前端展示）
     */
    private TeacherInfo teacher;

    /**
     * 教师信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String teacherNo;
        private String name;
        private String phone;
        private DepartmentInfo department;
    }

    /**
     * 院系信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }
}

