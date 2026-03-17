package com.example.demo.mapper;

import com.example.demo.dto.ChatSessionSummaryVO;
import com.example.demo.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ChatRecordMapper {
    /**
     * 存储大模型对话
     */
    int insert(ChatRecord chatRecord);

    // 1. 查询用户的所有会话摘要（接口1）
    List<ChatSessionSummaryVO> selectSessionSummaryByUserId(@Param("userId") Long userId);

    // 2. 查询指定会话的所有消息（接口2）
    List<ChatRecord> selectMessagesBySessionId(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId
    );

    // 3. 查询单条消息详情（接口3）
    ChatRecord selectById(@Param("id") Long id);
}