package com.example.demo.dto;

import lombok.Data;

import java.util.Date;

/**
 * 会话VO：前端展示的会话分组结构
 */
@Data
public class ChatSessionSummaryVO {
    private String sessionId;        // 会话ID
    private Long userId;             // 用户ID
    private String firstQuestion;    // 会话首条提问（用于会话标题）
    private Date createTime;         // 会话创建时间（首条消息时间）
    private Date lastUpdateTime;     // 会话最后更新时间
}
