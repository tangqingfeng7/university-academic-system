package com.university.academic.entity.attendance;

/**
 * 预警状态枚举
 */
public enum WarningStatus {
    /**
     * 待处理
     */
    PENDING("待处理"),
    
    /**
     * 已处理
     */
    HANDLED("已处理"),
    
    /**
     * 已忽略
     */
    IGNORED("已忽略");
    
    private final String description;
    
    WarningStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

