package com.university.academic.entity;

/**
 * 考试状态枚举
 *
 * @author Academic System Team
 */
public enum ExamStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    IN_PROGRESS("进行中"),
    FINISHED("已结束"),
    CANCELLED("已取消");

    private final String description;

    ExamStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

