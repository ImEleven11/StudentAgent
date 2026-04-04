package com.uestc.studentagent.backend.chat.mapper;

import com.uestc.studentagent.backend.chat.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    List<ChatMessageEntity> findBySessionId(@Param("sessionId") String sessionId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    long countBySessionId(@Param("sessionId") String sessionId);

    int insert(ChatMessageEntity message);

    int deleteBySessionId(@Param("sessionId") String sessionId);
}
