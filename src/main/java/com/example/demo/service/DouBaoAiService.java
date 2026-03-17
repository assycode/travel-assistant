package com.example.demo.service;

import com.example.demo.entity.ChatRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ChatRecordMapper;
import com.example.demo.service.impl.ChatService;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import com.example.demo.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DouBaoAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 注入ChatService负责对话存储
    @Autowired
    private ChatService chatService;

    // 备用Mapper（按需选择）
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Value("${doubao.api.key}")
    private String API_KEY;
    @Value("${doubao.api.url}")
    private String API_URL;
    @Value("${doubao.model}")
    private String model;

    // 构造函数注入
    public DouBaoAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 核心方法：调用AI并返回标准格式，自动从Token中获取userId存储对话
     * @param userInput 用户提问
     * @return 统一封装的 Result
     */
    public Result<Map<String, String>> getAiResponse(String userInput,String sessionId) {
        // 声明对话记录对象
        ChatRecord chatRecord = new ChatRecord();
        try {
            // 1. 从Request上下文获取Token解析后的userId（核心修改）
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Long userId = (Long) request.getAttribute("userId"); // 对应拦截器中存入的Long类型userId

            // 2. 前置参数校验（包含userId和userInput）
            if (userId == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录，无法获取用户ID");
            }
            if (userInput == null || userInput.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "提问内容不能为空");
            }
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = SessionUtil.generateSessionId();
            }

            // 3. 初始化对话记录（自动填充userId）
            chatRecord.setUserId(userId);
            chatRecord.setQuestion(userInput);
            chatRecord.setCreateTime(new java.util.Date());
            chatRecord.setSessionId(sessionId); // 绑定会话ID

            // 4. 构造AI请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            // 5. 构造AI请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            // 6. 序列化请求体
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

            // 7. 调用AI接口
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            // 8. 校验接口响应状态
            if (!response.getStatusCode().is2xxSuccessful()) {
                chatRecord.setAnswer("AI接口调用失败，状态码：" + response.getStatusCode());
                chatService.saveChatRecord(chatRecord);
                throw new BusinessException(ErrorCode.NETWORK_ERROR);
            }

            // 9. 解析AI响应内容
            String aiAnswer = extractContent(response.getBody());
            if (aiAnswer.startsWith("解析响应失败") || aiAnswer.startsWith("无响应内容")) {
                chatRecord.setAnswer(aiAnswer);
                chatService.saveChatRecord(chatRecord);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, aiAnswer);
            }

            // 10. 完善对话记录并存储
            chatRecord.setAnswer(aiAnswer);
            Long recordId = chatService.saveChatRecord(chatRecord);
            System.out.println("对话记录存储成功，ID：" + recordId + "，用户ID：" + userId);

            // 11. 封装返回结果
            Map<String, String> data = new HashMap<>();
            data.put("answer", aiAnswer);
            data.put("sessionId", sessionId); // 返回会话ID给前端
            return Result.success(data, "操作成功");

        } catch (BusinessException e) {
            // 业务异常处理
            chatRecord.setAnswer("业务异常：" + e.getMessage());
            try {
                chatService.saveChatRecord(chatRecord);
            } catch (Exception ex) {
                System.err.println("异常对话记录存储失败：" + ex.getMessage());
            }
            return Result.fail(e.getErrorCode(), e.getMessage());

        } catch (JsonProcessingException e) {
            // JSON序列化异常
            chatRecord.setAnswer("JSON解析失败：" + e.getMessage());
            try {
                chatService.saveChatRecord(chatRecord);
            } catch (Exception ex) {
                System.err.println("异常对话记录存储失败：" + ex.getMessage());
            }
            return Result.fail(ErrorCode.SYSTEM_ERROR, "JSON序列化失败：" + e.getMessage());

        } catch (Exception e) {
            // 未知异常兜底
            chatRecord.setAnswer("系统异常：" + e.getMessage());
            try {
                chatService.saveChatRecord(chatRecord);
            } catch (Exception ex) {
                System.err.println("异常对话记录存储失败：" + ex.getMessage());
            }
            e.printStackTrace();
            return Result.fail(ErrorCode.SYSTEM_ERROR, "AI服务调用失败：" + e.getMessage());
        }
    }

    /**
     * 解析AI响应内容（提取answer）
     */
    private String extractContent(String json) {
        if (json == null || json.isEmpty()) {
            return "无响应内容";
        }
        try {
            Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return message != null ? (String) message.get("content") : "无有效回答";
            } else {
                return "无有效回答，原始响应：" + json;
            }
        } catch (Exception e) {
            return "解析响应失败：" + e.getMessage() + "，原始响应：" + json;
        }
    }


}