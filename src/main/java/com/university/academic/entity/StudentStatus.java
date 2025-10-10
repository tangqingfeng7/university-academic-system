package com.university.academic.entity;

/**
 * 学生状态枚举
 *
 * @author Academic System Team
 */
public enum StudentStatus {
    /**
     * 在读
     */
    ACTIVE("在读"),
    
    /**
     * 休学
     */
    SUSPENDED("休学"),
    
    /**
     * 退学
     */
    WITHDRAWN("退学"),
    
    /**
     * 毕业
     */
    GRADUATED("毕业");

    private final String description;

    StudentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

