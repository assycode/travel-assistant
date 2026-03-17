package com.example.demo.controller;


import com.example.demo.dto.TripPlanListVO;
import com.example.demo.entity.TripGenerateRequest;
import com.example.demo.entity.TripPlan;
import com.example.demo.service.impl.TripService;
import com.example.demo.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trip")
public class TripController {

    @Autowired
    private TripService tripService;

    /**
     * 获取当前用户的行程列表
     * @param request 从Token中获取userId
     * @return 行程ID、名称、创建时间
     */
    @GetMapping("/list")
    public Result<List<TripPlanListVO>> getTripList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return tripService.getTripList(userId);
    }

    /**
     * 获取单个行程详情
     * @param id 行程ID（路径参数）
     * @return 完整行程信息
     */
    @GetMapping("/detail/{id}")
    public Result<TripPlan> getTripDetail(@PathVariable Long id) {
        return tripService.getTripDetail(id);
    }

    /**
     * AI生成旅游行程接口
     * @param request 前端传入的行程参数
     * @return 生成的行程详情（已保存到数据库）
     */
    @PostMapping("/ai-generate")
    public Result<TripPlan> aiGenerateTrip(@RequestBody TripGenerateRequest request) {
        return tripService.generateTrip(request);
    }
}
