package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TripPlan {
    private Long id;             // 行程ID
    private Long userId;         // 用户ID
    private String planName;     // 行程名称
    private String region;       // 目的地
    private BigDecimal budget;   // 预算金额
    private String playType;     // 游乐方式
    private String planContent;  // 行程内容
    private String planType;     // 行程类型：AI_GENERATE/CUSTOM
    private Date createTime;     // 创建时间
    private Date updateTime;     // 更新时间
}