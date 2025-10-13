package com.university.academic.entity.enums;

import lombok.Getter;

/**
 * 申诉状态枚举
 *
 * @author Academic System Team
 */
@Getter
public enum AppealStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    CANCELLED("已撤销");

    private final String description;

    AppealStatus(String description) {
        this.description = description;
    }
}

