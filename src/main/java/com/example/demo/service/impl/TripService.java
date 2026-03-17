package com.example.demo.service.impl;


import com.example.demo.dto.TripPlanListVO;
import com.example.demo.entity.TripGenerateRequest;
import com.example.demo.entity.TripPlan;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TripPlanMapper;
import com.example.demo.service.DouBaoAiService;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import com.example.demo.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 行程功能核心服务
 */
@Service
public class TripService {

    @Autowired
    private TripPlanMapper tripPlanMapper;

    @Autowired
    private DouBaoAiService douBaoAiService;

    public Result<List<TripPlanListVO>> getTripList(Long userId) {
        if (userId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        List<TripPlanListVO> list = tripPlanMapper.selectListByUserId(userId);
        return Result.success(list);
    }

    public Result<TripPlan> getTripDetail(Long id) {
        if (id == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "行程ID不能为空");
        }
        TripPlan tripPlan = tripPlanMapper.selectDetailById(id);
        if (tripPlan == null) {
            return Result.fail(ErrorCode.SYSTEM_ERROR, "行程不存在");
        }
        return Result.success(tripPlan);
    }

    public Result<TripPlan> generateTrip(TripGenerateRequest request) {
        try {
            // 1. 从Token获取当前用户ID
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录");
            }

            // 2. 校验前端参数
            if (request.getCity() == null || request.getCity().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "城市不能为空");
            }
            if (request.getDays() == null || request.getDays() <= 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "游玩天数必须大于0");
            }
            if (request.getMinBudget() == null || request.getMaxBudget() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "预算范围不能为空");
            }
            if (request.getMinBudget() > request.getMaxBudget()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "最小预算不能大于最大预算");
            }

            // 3. 包装成AI提问内容
            String prompt = buildTripPrompt(request);
            String sessionId = SessionUtil.generateSessionId(); // 生成会话ID

            // 4. 调用DouBaoAiService生成行程
            Result<Map<String, String>> aiResult = douBaoAiService.getAiResponse(prompt, sessionId);
            if (!aiResult.getSuccess()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成行程失败：" + aiResult.getMessage());
            }
            String aiAnswer = aiResult.getData().get("answer");

            // 5. 构造并保存行程记录到 trip_plan 表
            TripPlan tripPlan = new TripPlan();
            tripPlan.setUserId(userId);
            tripPlan.setPlanName(request.getCity() + request.getDays() + "日游");
            tripPlan.setRegion(request.getCity());
            // 预算：取中间值（也可直接存范围字符串）
            BigDecimal budget = BigDecimal.valueOf((request.getMinBudget() + request.getMaxBudget()) / 2.0);
            tripPlan.setBudget(budget);
            // 兴趣标签拼接为字符串
            String playType = String.join("、", request.getInterests());
            tripPlan.setPlayType(playType);
            // AI生成的行程内容
            tripPlan.setPlanContent(aiAnswer);
            tripPlan.setCreateTime(new Date());
            tripPlan.setUpdateTime(new Date());

            // 6. 保存到数据库
            int insert = tripPlanMapper.insert(tripPlan);
            if (insert == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "行程记录保存失败");
            }

            return Result.success(tripPlan, "行程生成成功");

        } catch (BusinessException e) {
            return Result.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(ErrorCode.SYSTEM_ERROR, "行程生成失败：" + e.getMessage());
        }
    }

    /**
     * 构建给AI的提示词
     */
    private String buildTripPrompt(TripGenerateRequest request) {
        return String.format(
                "请为我生成一份详细的旅游行程规划：\n" +
                        "目的地：%s\n" +
                        "游玩天数：%d天\n" +
                        "兴趣偏好：%s\n" +
                        "预算范围：%d-%d元\n" +
                        "其他需求：%s\n" +
                        "要求：行程按天规划，内容具体，符合预算，适合家庭出行，语言简洁明了。",
                request.getCity(),
                request.getDays(),
                String.join("、", request.getInterests()),
                request.getMinBudget(),
                request.getMaxBudget(),
                request.getOtherRequirements()
        );
    }


}