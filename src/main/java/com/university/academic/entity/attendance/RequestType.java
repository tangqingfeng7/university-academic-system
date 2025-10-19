package com.university.academic.entity.attendance;

/**
 * 考勤申请类型枚举
 */
public enum RequestType {
    /**
     * 补签申请
     */
    MAKEUP("补签申请"),
    
    /**
     * 考勤申诉
     */
    APPEAL("考勤申诉");
    
    private final String description;
    
    RequestType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

