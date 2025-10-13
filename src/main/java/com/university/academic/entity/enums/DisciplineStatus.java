package com.university.academic.entity.enums;

import lombok.Getter;

/**
 * 处分状态枚举
 *
 * @author Academic System Team
 */
@Getter
public enum DisciplineStatus {
    ACTIVE("有效"),
    REMOVED("已解除"),
    EXPIRED("已过期"),
    APPEALING("申诉中");

    private final String description;

    DisciplineStatus(String description) {
        this.description = description;
    }
}

