package com.university.academic.exception.attendance;

import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;

/**
 * 考勤模块基础异常类
 * 所有考勤相关的异常都继承自此类
 *
 * @author Academic System Team
 */
public class AttendanceException extends BusinessException {

    /**
     * 构造函数（使用错误码枚举）
     */
    public AttendanceException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 构造函数（使用错误码枚举和自定义消息）
     */
    public AttendanceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数（使用错误码枚举和异常原因）
     */
    public AttendanceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

