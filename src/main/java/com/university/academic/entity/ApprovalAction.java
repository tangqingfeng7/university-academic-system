package com.university.academic.entity;

/**
 * 审批操作枚举
 *
 * @author Academic System Team
 */
public enum ApprovalAction {
    /**
     * 批准
     */
    APPROVE("批准"),
    
    /**
     * 拒绝
     */
    REJECT("拒绝"),
    
    /**
     * 退回修改
     */
    RETURN("退回修改");

    private final String description;

    ApprovalAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

