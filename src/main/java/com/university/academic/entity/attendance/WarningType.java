package com.university.academic.entity.attendance;

/**
 * 考勤预警类型枚举
 */
public enum WarningType {
    /**
     * 学生旷课预警
     */
    STUDENT_ABSENT("学生旷课预警"),
    
    /**
     * 课程出勤率低预警
     */
    COURSE_LOW_RATE("课程出勤率低预警"),
    
    /**
     * 教师未考勤预警
     */
    TEACHER_NO_ATTENDANCE("教师未考勤预警");
    
    private final String description;
    
    WarningType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

