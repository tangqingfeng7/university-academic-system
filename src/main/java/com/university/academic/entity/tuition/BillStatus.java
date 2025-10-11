package com.university.academic.entity.tuition;

/**
 * 账单状态枚举
 *
 * @author Academic System Team
 */
public enum BillStatus {
    /**
     * 未支付
     */
    UNPAID("未支付"),
    
    /**
     * 部分支付
     */
    PARTIAL("部分支付"),
    
    /**
     * 已支付
     */
    PAID("已支付"),
    
    /**
     * 逾期
     */
    OVERDUE("逾期");

    private final String description;

    BillStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

