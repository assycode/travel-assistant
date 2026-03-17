package com.example.demo.dto;

import lombok.Data;

/**
 * 用户个人信息响应 VO
 */
@Data
public class UserProfileVO {
    private String name;        // 昵称（对应 nickname）
    private String phone;       // 手机号
    private String avatar;      // 头像 OSS 路径
    private Integer collectionCount; // 收藏景点数量
    private Integer planCount;  // 行程数量
}
