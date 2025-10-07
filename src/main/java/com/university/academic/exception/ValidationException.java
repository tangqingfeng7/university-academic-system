package com.university.academic.exception;

import lombok.Getter;

/**
 * 参数验证异常类
 * 用于处理参数校验失败的情况
 *
 * @author Academic System Team
 */
@Getter
public class ValidationException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数（使用默认错误码）
     */
    public ValidationException(String message) {
        super(message);
        this.code = ErrorCode.PARAM_ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造函数（自定义错误码和消息）
     */
    public ValidationException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数（使用错误码枚举）
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}

