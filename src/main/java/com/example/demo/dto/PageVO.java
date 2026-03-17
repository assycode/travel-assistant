package com.example.demo.dto;


import lombok.Data;
import java.util.List;

/**
 * 自定义分页响应类（替代 MyBatis-Plus 的 IPage）
 */
@Data
public class PageVO<T> {
    // 当前页码
    private Integer pageNum;
    // 每页条数
    private Integer pageSize;
    // 总记录数
    private Long total;
    // 总页数
    private Integer pages;
    // 当前页数据
    private List<T> records;

    // 构造方法：计算总页数
    public PageVO(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        // 计算总页数：向上取整
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
}
