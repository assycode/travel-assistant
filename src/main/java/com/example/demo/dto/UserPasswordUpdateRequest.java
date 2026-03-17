package com.example.demo.dto;

import lombok.Data;

/**
 * 修改密码请求 DTO
 */
@Data
public class UserPasswordUpdateRequest {
    private String oldPwd;
    private String newPwd;
}
