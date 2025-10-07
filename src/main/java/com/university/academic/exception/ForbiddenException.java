package com.university.academic.exception;

import lombok.Getter;

/**
 * 禁止访问异常类
 * 用于处理权限不足的情况
 *
 * @author Academic System Team
 */
@Getter
public class ForbiddenException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数（使用默认消息）
     */
    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN.getMessage());
        this.code = ErrorCode.FORBIDDEN.getCode();
        this.message = ErrorCode.FORBIDDEN.getMessage();
    }

    /**
     * 构造函数（自定义消息）
     */
    public ForbiddenException(String message) {
        super(message);
        this.code = ErrorCode.FORBIDDEN.getCode();
        this.message = message;
    }

    /**
     * 构造函数（使用错误码枚举）
     */
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}

