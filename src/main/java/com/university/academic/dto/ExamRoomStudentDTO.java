package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考场学生分配数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoomStudentDTO {

    /**
     * 分配记录ID
     */
    private Long id;

    /**
     * 考场ID
     */
    private Long examRoomId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 完整学生信息（用于前端展示）
     */
    private StudentInfo student;

    /**
     * 学生信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String studentNo;
        private String name;
        private String className;
        private MajorInfo major;
    }

    /**
     * 专业信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorInfo {
        private Long id;
        private String name;
    }
}

