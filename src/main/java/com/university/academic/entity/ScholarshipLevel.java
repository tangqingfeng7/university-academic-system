package com.university.academic.entity;

/**
 * 奖学金等级枚举
 */
public enum ScholarshipLevel {
    /**
     * 国家级奖学金
     */
    NATIONAL("国家级"),
    
    /**
     * 省级奖学金
     */
    PROVINCIAL("省级"),
    
    /**
     * 校级奖学金
     */
    UNIVERSITY("校级"),
    
    /**
     * 院系级奖学金
     */
    DEPARTMENT("院系级");
    
    private final String description;
    
    ScholarshipLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

