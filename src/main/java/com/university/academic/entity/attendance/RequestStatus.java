package com.university.academic.entity.attendance;

/**
 * 申请状态枚举
 */
public enum RequestStatus {
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
    REJECTED("已拒绝");
    
    private final String description;
    
    RequestStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

