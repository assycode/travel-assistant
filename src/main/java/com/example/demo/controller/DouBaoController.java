package com.example.demo.controller;


import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatSessionSummaryVO;
import com.example.demo.dto.PageVO;
import com.example.demo.entity.ChatRecord;
import com.example.demo.service.DouBaoAiService;
import com.example.demo.service.impl.ChatService;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class DouBaoController {

    @Autowired
    private DouBaoAiService douBaoAiService;

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public Result<Map<String, String>> getAIResponse(@RequestBody ChatRequest userInput) {
        try {
            return douBaoAiService.getAiResponse(userInput.getContent(),userInput.getSessionId());
        } catch (Exception e) {
            // 异常时也返回 Result 格式，保持前后端统一
            return Result.fail(ErrorCode.SYSTEM_ERROR, "AI服务异常：" + e.getMessage());
        }
    }

    /**
     * 接口1：获取当前用户的所有会话摘要
     * 无需传参，从Token中获取userId
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionSummaryVO>> getSessionSummaries(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.getSessionSummaries(userId);
    }

    /**
     * 接口2：获取指定会话的消息列表
     * @param sessionId 会话ID（路径参数）
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<List<ChatRecord>> getMessagesBySessionId(
            HttpServletRequest request,
            @PathVariable String sessionId
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.getMessagesBySessionId(userId, sessionId);
    }

    /**
     * 接口3：获取单条消息详情
     * @param id 消息ID（路径参数）
     */
    @GetMapping("/messages/{id}")
    public Result<ChatRecord> getMessageById(@PathVariable Long id) {
        return chatService.getMessageById(id);
    }


}