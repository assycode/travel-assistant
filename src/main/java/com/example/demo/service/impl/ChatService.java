package com.example.demo.service.impl;


import com.example.demo.dto.ChatSessionSummaryVO;
import com.example.demo.dto.PageVO;
import com.example.demo.entity.ChatRecord;
import com.example.demo.mapper.ChatRecordMapper;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long saveChatRecord(ChatRecord chatRecord) {
        chatRecordMapper.insert(chatRecord);
        return chatRecord.getId();
    }
    // 接口1：获取会话摘要列表
    public Result<List<ChatSessionSummaryVO>> getSessionSummaries(Long userId) {
        if (userId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        List<ChatSessionSummaryVO> summaries = chatRecordMapper.selectSessionSummaryByUserId(userId);
        return Result.success(summaries);
    }

    // 接口2：获取会话内消息
    public Result<List<ChatRecord>> getMessagesBySessionId(Long userId, String sessionId) {
        if (userId == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (!StringUtils.hasText(sessionId)) {
            return Result.fail(ErrorCode.PARAM_ERROR, "sessionId不能为空");
        }
        List<ChatRecord> messages = chatRecordMapper.selectMessagesBySessionId(userId, sessionId);
        return Result.success(messages);
    }

    // 接口3：获取单条消息详情

    public Result<ChatRecord> getMessageById(Long id) {
        if (id == null) {
            return Result.fail(ErrorCode.PARAM_ERROR, "消息ID不能为空");
        }
        ChatRecord record = chatRecordMapper.selectById(id);
        if (record == null) {
            return Result.fail(ErrorCode.SYSTEM_ERROR, "消息不存在");
        }
        return Result.success(record);
    }
}
