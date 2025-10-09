package com.university.academic.entity;

/**
 * 考试类型枚举
 *
 * @author Academic System Team
 */
public enum ExamType {
    MIDTERM("期中考试"),
    FINAL("期末考试"),
    MAKEUP("补考"),
    RETAKE("重修考试");

    private final String description;

    ExamType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

