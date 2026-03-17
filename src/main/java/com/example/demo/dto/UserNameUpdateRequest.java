package com.example.demo.dto;

import lombok.Data;

/**
 * 修改昵称请求 DTO
 */
@Data
public class UserNameUpdateRequest {
    private String newName;
}
