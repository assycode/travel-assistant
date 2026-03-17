package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ScenicSpot {
    private Long id;             // 景点ID
    private String name;         // 景点名称
    private String region;       // 地域
    private String category;     // 景点类别
    private String description;  // 详情描述
    private String address;      // 详细地址
    private BigDecimal price;    // 门票价格
    private BigDecimal rating;   // 评分
    private String coverImageUrl;// 封面图片URL
    private Date createTime;     // 创建时间
    private Date updateTime;     // 更新时间

}
