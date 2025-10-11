package com.university.academic.entity.tuition;

/**
 * 退费类型枚举
 *
 * @author Academic System Team
 */
public enum RefundType {
    /**
     * 全额退费
     */
    FULL("全额退费"),
    
    /**
     * 部分退费
     */
    PARTIAL("部分退费");

    private final String description;

    RefundType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

