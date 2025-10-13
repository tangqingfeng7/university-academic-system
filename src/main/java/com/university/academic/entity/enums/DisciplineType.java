package com.university.academic.entity.enums;

import lombok.Getter;

/**
 * 处分类型枚举
 *
 * @author Academic System Team
 */
@Getter
public enum DisciplineType {
    WARNING("警告"),
    SERIOUS_WARNING("严重警告"),
    DEMERIT("记过"),
    SERIOUS_DEMERIT("记大过"),
    PROBATION("留校察看"),
    EXPULSION("开除学籍");

    private final String description;

    DisciplineType(String description) {
        this.description = description;
    }
}

