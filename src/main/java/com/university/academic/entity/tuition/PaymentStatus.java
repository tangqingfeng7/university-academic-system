package com.university.academic.entity.tuition;

/**
 * 支付状态枚举
 *
 * @author Academic System Team
 */
public enum PaymentStatus {
    /**
     * 待支付
     */
    PENDING("待支付"),
    
    /**
     * 支付成功
     */
    SUCCESS("支付成功"),
    
    /**
     * 支付失败
     */
    FAILED("支付失败"),
    
    /**
     * 已退款
     */
    REFUNDED("已退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

