package com.university.academic.entity.attendance;

/**
 * 考勤记录状态枚举
 */
public enum RecordStatus {
    /**
     * 进行中
     */
    IN_PROGRESS("进行中"),
    
    /**
     * 已提交
     */
    SUBMITTED("已提交"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    RecordStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

