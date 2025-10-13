package com.university.academic.entity.enums;

import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author Academic System Team
 */
@Getter
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
    REJECTED("已拒绝");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }
}

