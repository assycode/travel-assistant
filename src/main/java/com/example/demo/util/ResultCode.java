package com.example.demo.util;

/**
 * 返回结果状态码枚举
 */
public enum ResultCode {
    // 成功
    SUCCESS("200", "请求成功"),
    // 失败
    FAIL("500", "请求失败"),
    // 未授权
    UNAUTHORIZED("401", "未授权"),
    // 资源不存在
    NOT_FOUND("404", "资源不存在"),
    // 限流
    TOO_MANY_REQUESTS("429", "请求过于频繁，请稍后再试");

    private final String code;
    private final String msg;

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
