package com.university.academic.entity.attendance;

/**
 * 考勤状态枚举
 */
public enum AttendanceStatus {
    /**
     * 出勤
     */
    PRESENT("出勤"),
    
    /**
     * 迟到
     */
    LATE("迟到"),
    
    /**
     * 早退
     */
    EARLY_LEAVE("早退"),
    
    /**
     * 请假
     */
    LEAVE("请假"),
    
    /**
     * 旷课
     */
    ABSENT("旷课");
    
    private final String description;
    
    AttendanceStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

