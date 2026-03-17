package com.example.demo.dto;

import com.example.demo.entity.ScenicSpot;
import lombok.Data;

import java.util.List;

@Data
public class ScenicSpotPageVO {
    // 景点列表
    private List<ScenicSpot> list;
    // 总条数
    private Long total;
    // 当前页码
    private Integer currentPage;
}
