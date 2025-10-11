package com.university.academic.entity.tuition;

/**
 * 支付方式枚举
 *
 * @author Academic System Team
 */
public enum PaymentMethod {
    /**
     * 支付宝
     */
    ALIPAY("支付宝"),
    
    /**
     * 微信支付
     */
    WECHAT("微信支付"),
    
    /**
     * 银行卡
     */
    BANK_CARD("银行卡"),
    
    /**
     * 现金
     */
    CASH("现金"),
    
    /**
     * 银行转账
     */
    BANK_TRANSFER("银行转账");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

