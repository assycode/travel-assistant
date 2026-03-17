package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}