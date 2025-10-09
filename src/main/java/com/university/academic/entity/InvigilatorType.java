package com.university.academic.entity;

/**
 * 监考类型枚举
 *
 * @author Academic System Team
 */
public enum InvigilatorType {
    CHIEF("主监考"),
    ASSISTANT("副监考");

    private final String description;

    InvigilatorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

