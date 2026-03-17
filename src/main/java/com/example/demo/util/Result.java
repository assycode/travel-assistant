package com.example.demo.util;

import lombok.Data;

/**
 * 通用返回结果封装
 * @param <T> 数据泛型
 */
@Data
public class Result<T> {
    // 全局返回码（ResultCode）
    private String resultCode;
    // 业务错误码（ErrorCode）
    private String errorCode;
    // 返回数据
    private T data;
    // 错误信息/提示信息
    private String message;
    // 请求是否成功
    private boolean success;

    // ========== 手动补充 getSuccess() 方法（核心修复） ==========
    public boolean getSuccess() {
        return this.success;
    }

    // ========== 快速构建方法 ==========
    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setResultCode(ResultCode.SUCCESS.getCode());
        result.setErrorCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        result.setSuccess(true);
        return result;
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义提示）
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回（通用）
     */
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setResultCode(ResultCode.FAIL.getCode());
        result.setErrorCode(ErrorCode.SYSTEM_ERROR.getCode());
        result.setMessage(ResultCode.FAIL.getMsg());
        result.setSuccess(false);
        return result;
    }

    /**
     * 失败返回（指定错误码）
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setResultCode(ResultCode.FAIL.getCode());
        result.setErrorCode(errorCode.getCode());
        result.setMessage(errorCode.getMsg());
        result.setSuccess(false);
        return result;
    }

    /**
     * 失败返回（指定错误码 + 自定义提示）
     */
    public static <T> Result<T> fail(ErrorCode errorCode, String customMsg) {
        Result<T> result = fail(errorCode);
        result.setMessage(customMsg);
        return result;
    }

    /**
     * 失败返回（自定义全局码 + 错误码 + 提示）
     */
    public static <T> Result<T> fail(String resultCode, String errorCode, String message) {
        Result<T> result = new Result<>();
        result.setResultCode(resultCode);
        result.setErrorCode(errorCode);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
}