package com.university.academic.entity;

/**
 * 评价状态枚举
 *
 * @author Academic System Team
 */
public enum EvaluationStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交"),
    REVIEWED("已审阅");

    private final String description;

    EvaluationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

