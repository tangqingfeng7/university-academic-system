package com.university.academic.entity;

/**
 * 审批状态枚举
 *
 * @author Academic System Team
 */
public enum ApprovalStatus {
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

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

