package com.university.academic.entity.attendance;

/**
 * 考勤方式枚举
 */
public enum AttendanceMethod {
    /**
     * 手动点名
     */
    MANUAL("手动点名"),
    
    /**
     * 扫码签到
     */
    QRCODE("扫码签到"),
    
    /**
     * 定位签到
     */
    LOCATION("定位签到");
    
    private final String description;
    
    AttendanceMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

