package com.example.demo.entity;

import lombok.Data;

import java.util.List;

/**
 * 旅游行程生成请求 DTO
 */
@Data
public class TripGenerateRequest {
    private String city;                  // 城市（如：上海）
    private Integer days;                 // 游玩天数
    private List<String> interests;      // 兴趣标签（如：美食、娱乐）
    private Integer minBudget;           // 最小预算
    private Integer maxBudget;           // 最大预算
    private String otherRequirements;    // 其他需求（如：带老人小孩）
}