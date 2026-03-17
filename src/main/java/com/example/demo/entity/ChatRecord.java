package com.example.demo.entity;

import lombok.Data;
import java.util.Date;

@Data
public class ChatRecord {
    private Long id;             // 对话ID
    private Long userId;         // 用户ID
    private String question;     // 用户提问
    private String answer;       // AI回答
    private Long scenicSpotId;   // 关联景点ID
    private Date createTime;     // 对话时间
    private String sessionId; // 会话ID（新增）
}