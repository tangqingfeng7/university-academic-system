package com.university.academic.entity;

/**
 * 教室类型枚举
 */
public enum ClassroomType {
    /**
     * 普通教室
     */
    LECTURE("普通教室"),
    
    /**
     * 实验室
     */
    LAB("实验室"),
    
    /**
     * 多媒体教室
     */
    MULTIMEDIA("多媒体教室"),
    
    /**
     * 会议室
     */
    CONFERENCE("会议室");
    
    private final String description;
    
    ClassroomType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

