package com.uestc.studentagent.backend.chat.service.impl;

import com.uestc.studentagent.backend.chat.dto.ChatHistoryResponse;
import com.uestc.studentagent.backend.chat.dto.ChatMessageResponse;
import com.uestc.studentagent.backend.chat.dto.ChatReplyResponse;
import com.uestc.studentagent.backend.chat.dto.ChatSendRequest;
import com.uestc.studentagent.backend.chat.dto.ChatSessionStartResponse;
import com.uestc.studentagent.backend.chat.dto.CompleteMissingRequest;
import com.uestc.studentagent.backend.chat.dto.StartAssessmentRequest;
import com.uestc.studentagent.backend.chat.entity.ChatMessageEntity;
import com.uestc.studentagent.backend.chat.entity.ChatRole;
import com.uestc.studentagent.backend.chat.entity.ChatSessionEntity;
import com.uestc.studentagent.backend.chat.entity.ChatSessionStatus;
import com.uestc.studentagent.backend.chat.entity.ChatSessionType;
import com.uestc.studentagent.backend.chat.mapper.ChatMessageMapper;
import com.uestc.studentagent.backend.chat.mapper.ChatSessionMapper;
import com.uestc.studentagent.backend.chat.service.ChatService;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import com.uestc.studentagent.backend.profile.mapper.StudentProfileMapper;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserService userService;
    private final StudentProfileMapper studentProfileMapper;

    public ChatServiceImpl(ChatSessionMapper chatSessionMapper,
                           ChatMessageMapper chatMessageMapper,
                           UserService userService,
                           StudentProfileMapper studentProfileMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userService = userService;
        this.studentProfileMapper = studentProfileMapper;
    }

    @Override
    @Transactional
    public ChatReplyResponse send(Long userId, ChatSendRequest request) {
        userService.loadActiveUser(userId);
        String sessionId = resolveOrCreateSession(userId, request.getSessionId(), ChatSessionType.GENERAL);

        ChatMessageEntity userMessage = persistMessage(sessionId, userId, ChatRole.USER, request.getMessage());
        ChatMessageEntity aiMessage = persistMessage(sessionId, userId, ChatRole.AI, buildAiReply(userId, request.getMessage()));

        return new ChatReplyResponse(sessionId, ChatMessageResponse.from(aiMessage));
    }

    @Override
    public ChatHistoryResponse history(Long userId, String sessionId, Integer page, Integer size) {
        ChatSessionEntity session = requireOwnedSession(userId, sessionId);
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        int offset = (normalizedPage - 1) * normalizedSize;

        List<ChatMessageResponse> messages = chatMessageMapper.findBySessionId(session.getId(), offset, normalizedSize)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
        long total = chatMessageMapper.countBySessionId(session.getId());

        return new ChatHistoryResponse(session.getId(), normalizedPage, normalizedSize, total, messages);
    }

    @Override
    @Transactional
    public ChatSessionStartResponse startAssessment(Long userId, StartAssessmentRequest request) {
        userService.loadActiveUser(userId);
        String type = request.getType().toLowerCase(Locale.ROOT);
        String sessionId = createSession(userId, ChatSessionType.ASSESSMENT);
        String question = switch (type) {
            case "interest" -> "请告诉我，你最近最愿意主动投入时间学习或实践的方向是什么？";
            case "personality" -> "在团队协作中，你更偏向主导推进、协调沟通，还是独立完成关键任务？";
            case "risk" -> "面对有不确定性的岗位选择时，你更倾向稳妥路径还是高成长高波动路径？";
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST, "unsupported assessment type");
        };

        ChatMessageEntity firstMessage = persistMessage(sessionId, userId, ChatRole.AI, question);
        return new ChatSessionStartResponse(sessionId, ChatMessageResponse.from(firstMessage));
    }

    @Override
    @Transactional
    public ChatSessionStartResponse startMissingFieldCompletion(Long userId, CompleteMissingRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = studentProfileMapper.findById(request.getProfileId());
        if (profile == null || !profile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "profile not found");
        }

        String field = request.getMissingFields().get(0);
        String prompt = "为了补全画像信息，请先补充字段 " + field + " 的相关内容。";

        String sessionId = createSession(userId, ChatSessionType.COMPLETE_MISSING);
        ChatMessageEntity firstMessage = persistMessage(sessionId, userId, ChatRole.AI, prompt);
        return new ChatSessionStartResponse(sessionId, ChatMessageResponse.from(firstMessage));
    }

    @Override
    @Transactional
    public void clearSession(Long userId, String sessionId) {
        ChatSessionEntity session = requireOwnedSession(userId, sessionId);
        chatMessageMapper.deleteBySessionId(session.getId());
        chatSessionMapper.deleteById(session.getId());
    }

    private String resolveOrCreateSession(Long userId, String sessionId, ChatSessionType sessionType) {
        if (!StringUtils.hasText(sessionId)) {
            return createSession(userId, sessionType);
        }
        return requireOwnedSession(userId, sessionId).getId();
    }

    private String createSession(Long userId, ChatSessionType sessionType) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setSessionType(sessionType);
        session.setStatus(ChatSessionStatus.ACTIVE);
        chatSessionMapper.insert(session);
        return session.getId();
    }

    private ChatSessionEntity requireOwnedSession(Long userId, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "sessionId is required");
        }
        ChatSessionEntity session = chatSessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "chat session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "chat session does not belong to current user");
        }
        return session;
    }

    private ChatMessageEntity persistMessage(String sessionId, Long userId, ChatRole role, String content) {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        chatMessageMapper.insert(message);
        return message;
    }

    private String buildAiReply(Long userId, String userMessage) {
        StudentProfileEntity profile = studentProfileMapper.findByUserId(userId);
        if (profile == null) {
            return "我还没有读取到你的完整画像。你可以先上传简历或手动补充教育背景、技能和项目经历。";
        }

        boolean missingSkills = profile.getSkillsJson() == null || profile.getSkillsJson().equals("[]");
        boolean missingExperience = profile.getExperiencesJson() == null || profile.getExperiencesJson().equals("[]");

        if (missingSkills || missingExperience) {
            return "我已经记录你的消息：" + userMessage + "。当前建议先补充技能和项目经历，这样后续匹配和报告会更准确。";
        }

        return "我已经记录你的消息：" + userMessage + "。结合你当前画像，下一步我可以继续帮你细化目标岗位、能力差距和行动计划。";
    }
}
