package com.example.demo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserScenicCollect {
    private Long id;             // 主键ID
    private Long userId;         // 用户ID
    private Long scenicSpotId;   // 景点ID
    private Date createTime;     // 收藏时间
}
