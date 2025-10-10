package com.university.academic.entity;

/**
 * 学籍异动类型枚举
 *
 * @author Academic System Team
 */
public enum ChangeType {
    /**
     * 休学
     */
    SUSPENSION("休学"),
    
    /**
     * 复学
     */
    RESUMPTION("复学"),
    
    /**
     * 转专业
     */
    TRANSFER("转专业"),
    
    /**
     * 退学
     */
    WITHDRAWAL("退学");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

