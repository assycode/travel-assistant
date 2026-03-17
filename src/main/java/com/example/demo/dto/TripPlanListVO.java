package com.example.demo.dto;

import lombok.Data;

import java.util.Date;

/**
 * 行程列表精简VO
 */
@Data
public class TripPlanListVO {
    private Long id;          // 行程ID
    private String planName;  // 行程名称
    private Date createTime;  // 创建时间
}