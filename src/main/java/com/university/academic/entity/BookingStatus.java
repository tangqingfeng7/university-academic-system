package com.university.academic.entity;

/**
 * 教室借用状态枚举
 */
public enum BookingStatus {
    /**
     * 待审批
     */
    PENDING("待审批"),
    
    /**
     * 已批准
     */
    APPROVED("已批准"),
    
    /**
     * 已拒绝
     */
    REJECTED("已拒绝"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    BookingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

