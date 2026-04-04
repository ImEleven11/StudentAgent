package com.uestc.studentagent.backend.chat.mapper;

import com.uestc.studentagent.backend.chat.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatSessionMapper {

    ChatSessionEntity findById(@Param("id") String id);

    int insert(ChatSessionEntity session);

    int deleteById(@Param("id") String id);
}
