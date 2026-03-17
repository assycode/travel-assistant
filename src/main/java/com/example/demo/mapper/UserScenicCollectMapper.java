package com.example.demo.mapper;

import com.example.demo.entity.UserScenicCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserScenicCollectMapper {

        /**
         * 查询用户是否收藏了某景点
         */
        Integer countByUserAndScenic(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);

        /**
         * 收藏景点（新增关联记录）
         */
        int insert(UserScenicCollect collect);

        /**
         * 取消收藏（删除关联记录）
         */
        int deleteByUserAndScenic(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);

        /**
         * 查询用户收藏的所有景点ID
         */
        List<Long> selectScenicIdsByUserId(@Param("userId") Long userId);

        /**
         * 统计用户收藏景点数
         */
        Integer countByUserId(@Param("userId") Long userId);
}
