package com.example.demo.dto;


import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;  // 用户名
    private String password;   // 密码
    private String phone;     // 手机号
}
