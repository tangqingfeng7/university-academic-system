package com.university.academic.entity;

/**
 * 毕业审核状态枚举
 *
 * @author Academic System Team
 */
public enum AuditStatus {
    /**
     * 通过
     */
    PASS("通过"),
    
    /**
     * 不通过
     */
    FAIL("不通过"),
    
    /**
     * 待审核
     */
    PENDING("待审核"),
    
    /**
     * 延期毕业
     */
    DEFERRED("延期毕业");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

