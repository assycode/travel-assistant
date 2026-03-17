package com.example.demo.util;

/**
 * 错误码枚举（可按模块扩展）
 */
public enum ErrorCode {
    // ========== 系统级错误 ==========
    SUCCESS("00000", "操作成功"),
    SYSTEM_ERROR("99999", "系统内部错误"),
    PARAM_ERROR("00001", "参数非法"),
    NETWORK_ERROR("00002", "第三方接口调用失败"),

    // ========== 景点图片业务错误 ==========
    PLACE_NOT_FOUND("10001", "景点不存在，无法获取图片"),
    IMAGE_API_KEY_INVALID("10002", "图片API密钥无效/过期"),
    IMAGE_URL_EMPTY("10003", "景点图片URL为空"),
    IMAGE_DOWNLOAD_FAIL("10004", "图片下载失败"),
    IMAGE_STORE_FAIL("10005", "图片存储失败"),
    IMAGE_COPYRIGHT_ERROR("10006", "图片版权不符合要求");

    // 错误码
    private final String code;
    // 错误描述
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // getter 方法
    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
