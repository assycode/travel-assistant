package com.example.demo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 工具类
 * 支持生成、解析、验证、刷新Token
 */
public class TokenUtil {
    // ==================== 配置项（可放到配置文件中） ====================
    /**
     * 秘钥（建议至少32位，生产环境从配置中心/环境变量读取，不要硬编码）
     * 注意：HS256算法要求密钥长度≥256位（32字节），此处示例为32位字符串
     */
    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvwxyz1234567890abcdeffgfgfgfdssfgdytrytjhgjhg";
    /**
     * Token 过期时间（单位：秒），默认2小时
     */
    private static final long EXPIRATION_TIME = 7200L;
    /**
     * 刷新Token过期时间（单位：秒），默认7天
     */
    private static final long REFRESH_EXPIRATION_TIME = 604800L;
    /**
     * 签发者
     */
    private static final String ISSUER = "place-image-api";

    // 生成加密秘钥（使用JDK标准Base64解码，兼容URL安全模式）
    private static SecretKey getSecretKey() {
        try {
            // 替换Tomcat Base64为JDK标准Base64（URL安全模式，解决解码位非法问题）
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] keyBytes = decoder.decode(SECRET_KEY);

            // 校验密钥长度（HS256要求≥256位/32字节，不足则抛出异常）
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("密钥长度不足，HS256算法要求密钥至少32字节（256位）");
            }

            // 生成符合JJWT要求的HMAC SHA密钥
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // 处理Base64解码失败/密钥长度不足的情况
            throw new RuntimeException("生成JWT密钥失败：" + e.getMessage(), e);
        }
    }

    // ==================== 核心方法 ====================
    /**
     * 生成访问Token（Access Token）
     * @param userId    用户ID（自定义业务字段）
     * @param claims    自定义载荷（如角色、权限等）
     * @return  JWT Token字符串
     */
    public static String generateAccessToken(String userId, Map<String, Object> claims) {
        // 1. 先创建claims对象（避免覆盖默认字段）
        Claims jwtClaims = Jwts.claims();
        // 2. 放入自定义载荷（如果有）
        if (claims != null) {
            jwtClaims.putAll(claims);
        }
        // 3. 手动设置userId到claims（兼容解析逻辑）+ 保留sub字段
        jwtClaims.put("userId", userId);

        return Jwts.builder()
                // 唯一标识（JTI），用于注销Token
                .setId(UUID.randomUUID().toString())
                // 签发者
                .setIssuer(ISSUER)
                // 主题（用户ID）- 保留sub字段
                .setSubject(userId)
                // 自定义载荷（先设置自定义，再设置标准字段，避免覆盖）
                .setClaims(jwtClaims)
                // 签发时间
                .setIssuedAt(new Date())
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000))
                // 签名算法 + 秘钥（HS256）
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新Token（Refresh Token）
     * @param userId 用户ID
     * @return  Refresh Token字符串
     */
    public static String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(ISSUER)
                .setSubject(userId)
                // 刷新Token也放入userId字段，保持解析逻辑统一
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME * 1000))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token，获取载荷信息
     * @param token Token字符串
     * @return  JWT载荷（Claims）
     * @throws IllegalArgumentException Token为空
     * @throws ExpiredJwtException Token已过期
     * @throws SignatureException 签名错误
     * @throws MalformedJwtException Token格式错误
     */
    public static Claims parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 保留过期Token的载荷信息，便于业务处理
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token已过期", e);
        } catch (SignatureException e) {
            throw new SignatureException("Token签名验证失败", e);
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Token格式错误", e);
        } catch (Exception e) {
            throw new RuntimeException("解析Token失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证Token是否有效（未过期、签名正确）
     * @param token Token字符串
     * @return  true-有效，false-无效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
            // 捕获常见的JWT无效场景：过期、签名错误、格式错误、空Token
            return false;
        } catch (Exception e) {
            // 其他未知异常也判定为无效
            return false;
        }
    }

    /**
     * 刷新Access Token（通过有效的Refresh Token生成新的Access Token）
     * @param refreshToken 刷新Token
     * @param newClaims    新的自定义载荷（可选）
     * @return  新的Access Token
     * @throws Exception 刷新Token无效时抛出异常
     */
    public static String refreshAccessToken(String refreshToken, Map<String, Object> newClaims) throws Exception {
        if (!validateToken(refreshToken)) {
            throw new Exception("刷新Token无效或已过期");
        }
        // 解析Refresh Token获取用户ID
        Claims refreshClaims = parseToken(refreshToken);
        String userId = refreshClaims.getSubject();
        // 生成新的Access Token
        return generateAccessToken(userId, newClaims);
    }


    /**
     * 从Token中获取自定义字段
     * @param token Token字符串
     * @param key   字段名
     * @return  字段值
     */
    public static Object getClaimFromToken(String token, String key) {
        Claims claims = parseToken(token);
        return claims.get(key);
    }

    /**
     * 获取Token剩余过期时间（单位：秒）
     * @param token Token字符串
     * @return  剩余秒数（负数表示已过期）
     */
    public static long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    private static Claims parseTokenToClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 增加异常处理，避免解析失败时直接抛异常
            throw new RuntimeException("解析Token载荷失败：" + e.getMessage(), e);
        }
    }

}