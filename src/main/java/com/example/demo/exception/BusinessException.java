package com.example.demo.exception;

import com.example.demo.util.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类，用于抛出业务层面的错误
 */
@Getter
public class BusinessException extends RuntimeException {

    // 业务错误码枚举
    private final ErrorCode errorCode;

    /**
     * 构造方法：仅传入错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    /**
     * 构造方法：传入错误码枚举 + 自定义提示信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}