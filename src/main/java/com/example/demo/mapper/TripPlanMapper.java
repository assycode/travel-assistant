package com.example.demo.mapper;


import com.example.demo.dto.TripPlanListVO;
import com.example.demo.entity.TripPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 行程规划Mapper（纯MyBatis实现，无MyBatis-Plus）
 */
@Repository // 标识为持久层组件，确保Spring能扫描到
@Mapper
public interface TripPlanMapper {

    // 查询用户的行程列表（仅返回id、planName、createTime）
    List<TripPlanListVO> selectListByUserId(@Param("userId") Long userId);

    // 根据行程ID查询详情
    TripPlan selectDetailById(@Param("id") Long id);

    int insert(TripPlan tripPlan);

    // 统计用户行程数
    Integer countByUserId(@Param("userId") Long userId);
}
