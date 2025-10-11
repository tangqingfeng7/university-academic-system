package com.university.academic.entity;

/**
 * 奖学金申请状态枚举
 */
public enum ApplicationStatus {
    /**
     * 待审核
     */
    PENDING("待审核"),
    
    /**
     * 辅导员已批准
     */
    COUNSELOR_APPROVED("辅导员已批准"),
    
    /**
     * 院系已批准
     */
    DEPT_APPROVED("院系已批准"),
    
    /**
     * 已拒绝
     */
    REJECTED("已拒绝");
    
    private final String description;
    
    ApplicationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

