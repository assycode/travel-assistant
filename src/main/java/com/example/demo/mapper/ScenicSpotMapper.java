package com.example.demo.mapper;

import com.example.demo.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ScenicSpotMapper {
    /**
     * 根据ID查询景点详情
     */
    ScenicSpot selectById(@Param("id") Long id);

    /**
     * 获取地区
     * @return
     */
    List<String> getRegions();

    /**
     * 按城市分页查询景点
     * @param city 城市名称
     * @param offset 分页偏移量
     * @param pageSize 每页条数
     * @return 景点列表
     */
    List<ScenicSpot> selectByCityPage(
            @Param("city") String city,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 查询按城市筛选后的总条数
     * @param city 城市名称
     * @return 总条数
     */
    Long selectCountByCity(@Param("city") String city);


    Integer countById(Long scenicSpotId);
}