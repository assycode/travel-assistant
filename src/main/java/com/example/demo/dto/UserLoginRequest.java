package com.example.demo.dto;


import lombok.Data;

@Data
public class UserLoginRequest {

    private String userName;  // 用户名
    private String password;   // 密码

}
