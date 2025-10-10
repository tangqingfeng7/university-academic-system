package com.university.academic.entity;

/**
 * 教室状态枚举
 */
public enum ClassroomStatus {
    /**
     * 可用
     */
    AVAILABLE("可用"),
    
    /**
     * 维修中
     */
    MAINTENANCE("维修中"),
    
    /**
     * 停用
     */
    DISABLED("停用");
    
    private final String description;
    
    ClassroomStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

