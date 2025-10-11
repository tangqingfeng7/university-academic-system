package com.university.academic.entity.tuition;

/**
 * 退费方式枚举
 *
 * @author Academic System Team
 */
public enum RefundMethod {
    /**
     * 原路退回
     */
    ORIGINAL("原路退回"),
    
    /**
     * 银行转账
     */
    BANK_TRANSFER("银行转账"),
    
    /**
     * 现金退费
     */
    CASH("现金退费");

    private final String description;

    RefundMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

