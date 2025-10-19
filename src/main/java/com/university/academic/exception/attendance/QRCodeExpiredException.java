package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 二维码已过期异常
 * 当学生扫描过期的二维码时抛出
 *
 * @author Academic System Team
 */
public class QRCodeExpiredException extends AttendanceException {

    public QRCodeExpiredException() {
        super(ErrorCode.QRCODE_EXPIRED);
    }

    public QRCodeExpiredException(String message) {
        super(ErrorCode.QRCODE_EXPIRED, message);
    }
}

