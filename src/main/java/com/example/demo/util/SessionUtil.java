package com.example.demo.util;

import java.util.UUID;

// 工具类：生成会话ID
public class SessionUtil {
    public static String generateSessionId() {
        // 简化UUID（去掉横线），也可直接用 UUID.randomUUID().toString()
        return UUID.randomUUID().toString().replace("-", "");
    }
}
