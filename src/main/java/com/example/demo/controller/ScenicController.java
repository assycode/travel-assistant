package com.example.demo.controller;

import com.example.demo.dto.ScenicSpotPageVO;
import com.example.demo.entity.ScenicSpot;
import com.example.demo.service.impl.ScenicSpotService;
import com.example.demo.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scenic")
public class ScenicController {


    @Autowired
    private ScenicSpotService scenicSpotService;




    /**
     * 查询景点详情
     * @param scenicId
     * @return
     */
    @GetMapping("/detail/{scenicId}")
    public Result<ScenicSpot> getScenicDetail(@PathVariable Long scenicId) {
        ScenicSpot detail = scenicSpotService.getScenicDetail(scenicId);
        if (detail == null) {
            return Result.fail(com.example.demo.util.ErrorCode.PLACE_NOT_FOUND);
        }
        return Result.success(detail, "查询景点详情成功");
    }

    /**
     * 收藏/取消收藏景点（用户维度）
     * @param request 获取Token中的userId
     * @param scenicSpotId 景点ID
     * @return 最新收藏状态（true=已收藏，false=未收藏）
     */
    @PostMapping("/collect")
    public Result<Boolean> collectScenicSpot(
            HttpServletRequest request,
            @RequestParam Long scenicSpotId
    ) {
        // 从Token拦截器中获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        return scenicSpotService.toggleCollect(userId, scenicSpotId);
    }

    /**
     * 查询当前用户收藏的所有景点ID
     * @param request 获取Token中的userId
     * @return 收藏的景点ID列表
     */
    @GetMapping("/collect/list")
    public Result<List<Long>> getCollectScenicList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return scenicSpotService.getCollectScenicIds(userId);
    }

    /**
     * 获取地区
     * @return
     */
    @GetMapping("/regions")
    public Result<List<String>> getRegions(){
        List<String> regions=scenicSpotService.getRegions();
        return Result.success(regions,"获取景点地区成功");
    }

    /**
     * 获取景点list
     */
    @GetMapping("/region")
    public Result<ScenicSpotPageVO> getScenicSpot(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return scenicSpotService.getScenicSpotByCity(city, page, pageSize);
    }


}
