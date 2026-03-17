package com.example.demo.interceptor;

import com.example.demo.util.TokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 增强版JWT Token拦截器
 * 优化：修复重复解析Token、空指针、类型不匹配问题
 */
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    // Token续期阈值：剩余时间小于5分钟（300秒）时触发续期
    private static final long REFRESH_THRESHOLD = 300L;

    /**
     * 前置拦截：校验Token + 自动续期 + 解析userId
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取Token（格式：Bearer <token>）
        String authHeader = request.getHeader("Authorization");
        System.out.println("收到的Authorization头：" + authHeader);

        // 校验Token是否存在且格式正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            returnJson(response, HttpStatus.UNAUTHORIZED.value(), "未携带有效Token，请先登录");
            return false;
        }

        // 2. 提取Token（去掉Bearer前缀，trim避免空格）
        String token = authHeader.substring(7).trim();

        try {
            // 3. 只解析一次Token
            Claims claims = TokenUtil.parseToken(token);

            // 4. 从Claims中获取userId（优先自定义字段，兜底sub）
            String userIdStr = claims.get("userId", String.class);
            if (userIdStr == null) {
                userIdStr = claims.getSubject();
            }

            // 5. 校验userId是否为空 + 转换为Long类型（匹配Controller的类型）
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                returnJson(response, HttpStatus.UNAUTHORIZED.value(), "Token中未包含用户ID");
                return false;
            }
            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                returnJson(response, HttpStatus.UNAUTHORIZED.value(), "用户ID格式错误（非数字）");
                return false;
            }

            // 6. 检查Token剩余时间，触发自动续期
            long remainingTime = TokenUtil.getRemainingTime(token);
            if (remainingTime < REFRESH_THRESHOLD) {
                // 6.1 提取原Token的自定义载荷（排除标准字段）
                Map<String, Object> newClaims = new HashMap<>();
                claims.forEach((key, value) -> {
                    if (!"iat".equals(key) && !"exp".equals(key) && !"iss".equals(key) && !"jti".equals(key)) {
                        newClaims.put(key, value);
                    }
                });
                // 6.2 生成新的Access Token（续期）
                String newToken = TokenUtil.generateAccessToken(userIdStr, newClaims);
                // 6.3 新Token通过响应头返回给前端
                response.setHeader("New-Token", newToken);
                response.setHeader("Token-Refreshed", "true");
            }

            // 7. 将用户信息存入请求上下文（Long类型，匹配Controller）
            request.setAttribute("userId", userId); // 存入Long类型的userId
            request.setAttribute("userClaims", claims);

            // 8. 放行请求
            return true;
        } catch (ExpiredJwtException e) {
            returnJson(response, HttpStatus.UNAUTHORIZED.value(), "Token已过期，请重新登录");
            return false;
        } catch (SignatureException e) {
            returnJson(response, HttpStatus.UNAUTHORIZED.value(), "Token签名错误，可能被篡改");
            return false;
        } catch (Exception e) {
            returnJson(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Token解析失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 统一返回JSON格式错误响应
     */
    private void returnJson(HttpServletResponse response, int code, String msg) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(code);

        PrintWriter writer = response.getWriter();
        writer.write(String.format("{\"code\":%d,\"msg\":\"%s\"}", code, msg));
        writer.flush();
        writer.close();
    }
}