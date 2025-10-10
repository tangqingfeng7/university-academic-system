package com.university.academic.entity;

/**
 * 教室使用类型枚举
 */
public enum UsageType {
    /**
     * 课程
     */
    COURSE("课程"),
    
    /**
     * 考试
     */
    EXAM("考试"),
    
    /**
     * 借用
     */
    BOOKING("借用"),
    
    /**
     * 其他
     */
    OTHER("其他");
    
    private final String description;
    
    UsageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

