package com.university.academic.entity.enums;

import lombok.Getter;

/**
 * 申诉结果枚举
 *
 * @author Academic System Team
 */
@Getter
public enum AppealResult {
    ACCEPT("接受申诉，撤销处分"),
    PARTIALLY_ACCEPT("部分接受，减轻处分"),
    REJECT("拒绝申诉，维持原处分");

    private final String description;

    AppealResult(String description) {
        this.description = description;
    }
}

