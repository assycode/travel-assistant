package com.example.demo.dto;


import lombok.Data;

@Data
public class UserLoginResponse {

    private String token;         // JWT Token
    private Long userId;          // 用户ID
    private String userName;      // 用户名

}
