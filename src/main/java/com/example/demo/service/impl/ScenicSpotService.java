package com.example.demo.service.impl;


import com.example.demo.dto.ScenicSpotPageVO;
import com.example.demo.entity.ScenicSpot;
import com.example.demo.entity.UserScenicCollect;
import com.example.demo.mapper.ScenicSpotMapper;
import com.example.demo.mapper.UserScenicCollectMapper;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 景点功能核心服务
 */
@Service
public class ScenicSpotService {
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private UserScenicCollectMapper collectMapper;

    /**
     * 查看景点详情
     */
    public ScenicSpot getScenicDetail(Long scenicId) {
        return scenicSpotMapper.selectById(scenicId);
    }

    public List<String> getRegions() {
        return scenicSpotMapper.getRegions();
    }

    public Result<ScenicSpotPageVO> getScenicSpotByCity(String city, Integer page, Integer pageSize) {
        // 1. 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 2. 计算分页偏移量
        Integer offset = (page - 1) * pageSize;

        // 3. 查询数据
        List<ScenicSpot> scenicSpotList = scenicSpotMapper.selectByCityPage(city, offset, pageSize);
        Long total = scenicSpotMapper.selectCountByCity(city);

        // 4. 转换为VO（适配前端字段）
        ScenicSpotPageVO pageVO = new ScenicSpotPageVO();
        pageVO.setTotal(total);
        pageVO.setCurrentPage(page);
        pageVO.setList(scenicSpotList);

        // 5. 返回结果
        return Result.success(pageVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> toggleCollect(Long userId, Long scenicSpotId) {
        // 1. 参数校验
        if (userId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (scenicSpotId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "景点ID不能为空");
        }

        // 2. 校验景点是否存在
        Integer scenicExist = scenicSpotMapper.countById(scenicSpotId);
        if (scenicExist == null || scenicExist == 0) {
            return Result.fail(ErrorCode.SYSTEM_ERROR, "景点不存在");
        }

        // 3. 查询当前用户是否收藏该景点
        Integer collectCount = collectMapper.countByUserAndScenic(userId, scenicSpotId);
        boolean isCollected = collectCount != null && collectCount > 0;

        // 4. 切换收藏状态
        if (isCollected) {
            // 已收藏 → 取消收藏
            collectMapper.deleteByUserAndScenic(userId, scenicSpotId);
            return Result.success(false, "取消收藏成功");
        } else {
            // 未收藏 → 新增收藏
            UserScenicCollect collect = new UserScenicCollect();
            collect.setUserId(userId);
            collect.setScenicSpotId(scenicSpotId);
            collectMapper.insert(collect);
            return Result.success(true, "收藏成功");
        }
    }

    public Result<List<Long>> getCollectScenicIds(Long userId) {
        if (userId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        List<Long> scenicIds = collectMapper.selectScenicIdsByUserId(userId);
        return Result.success(scenicIds);
    }

}