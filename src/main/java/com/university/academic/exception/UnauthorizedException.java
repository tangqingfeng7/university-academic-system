package com.university.academic.exception;

import lombok.Getter;

/**
 * 未授权异常类
 * 用于处理未登录或token无效的情况
 *
 * @author Academic System Team
 */
@Getter
public class UnauthorizedException extends RuntimeException {

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
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
        this.code = ErrorCode.UNAUTHORIZED.getCode();
        this.message = ErrorCode.UNAUTHORIZED.getMessage();
    }

    /**
     * 构造函数（自定义消息）
     */
    public UnauthorizedException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
        this.message = message;
    }

    /**
     * 构造函数（使用错误码枚举）
     */
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}

