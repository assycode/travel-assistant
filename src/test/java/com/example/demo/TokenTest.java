package com.example.demo;

import com.example.demo.util.TokenUtil;

import java.util.HashMap;
import java.util.Map;

public class TokenTest {
    public static void main(String[] args) {
        // 1. 生成Token（模拟用户登录后生成）
        String userId = "user_123456";
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", "admin"); // 自定义角色
        claims.put("scope", "place:image:read"); // 自定义权限

        String accessToken = TokenUtil.generateAccessToken(userId, claims);
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);

        // 2. 验证Token有效性
        boolean isValid = TokenUtil.validateToken(accessToken);
        System.out.println("Token是否有效: " + isValid);

        // 3. 解析Token获取信息
        String userIdFromToken = TokenUtil.getUserIdFromToken(accessToken);
        String role = (String) TokenUtil.getClaimFromToken(accessToken, "role");
        long remainingTime = TokenUtil.getRemainingTime(accessToken);
        System.out.println("解析出的用户ID: " + userIdFromToken);
        System.out.println("解析出的角色: " + role);
        System.out.println("Token剩余过期时间（秒）: " + remainingTime);

        // 4. 刷新Access Token
        try {
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("role", "admin");
            String newAccessToken = TokenUtil.refreshAccessToken(refreshToken, newClaims);
            System.out.println("新的Access Token: " + newAccessToken);
        } catch (Exception e) {
            System.out.println("刷新Token失败: " + e.getMessage());
        }
    }
}