package com.uestc.studentagent.backend.assessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.assessment.dto.AssessmentAdjustRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentCreateRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentQuestionResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentResultResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentSubmitRequest;
import com.uestc.studentagent.backend.assessment.entity.AssessmentEntity;
import com.uestc.studentagent.backend.assessment.entity.AssessmentStatus;
import com.uestc.studentagent.backend.assessment.entity.AssessmentType;
import com.uestc.studentagent.backend.assessment.mapper.AssessmentMapper;
import com.uestc.studentagent.backend.assessment.service.AssessmentService;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.plan.dto.PlanAdjustRequest;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;
import com.uestc.studentagent.backend.plan.dto.PlanTaskAdjustRequest;
import com.uestc.studentagent.backend.plan.entity.PlanEntity;
import com.uestc.studentagent.backend.plan.mapper.PlanMapper;
import com.uestc.studentagent.backend.plan.mapper.PlanTaskMapper;
import com.uestc.studentagent.backend.plan.service.PlanService;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    private static final TypeReference<List<AssessmentQuestionResponse>> QUESTION_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AssessmentMapper assessmentMapper;
    private final PlanMapper planMapper;
    private final PlanTaskMapper planTaskMapper;
    private final PlanService planService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AssessmentServiceImpl(AssessmentMapper assessmentMapper,
                                 PlanMapper planMapper,
                                 PlanTaskMapper planTaskMapper,
                                 PlanService planService,
                                 UserService userService,
                                 ObjectMapper objectMapper) {
        this.assessmentMapper = assessmentMapper;
        this.planMapper = planMapper;
        this.planTaskMapper = planTaskMapper;
        this.planService = planService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AssessmentResponse create(Long userId, AssessmentCreateRequest request) {
        userService.loadActiveUser(userId);
        PlanEntity plan = requireOwnedPlan(userId, request.getPlanId());

        AssessmentEntity entity = new AssessmentEntity();
        entity.setUserId(userId);
        entity.setPlanId(plan.getId());
        entity.setAssessmentType(parseType(request.getType()));
        entity.setStatus(AssessmentStatus.CREATED);
        entity.setQuestionsJson(toJson(buildQuestions(entity.getAssessmentType())));
        assessmentMapper.insert(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public AssessmentResponse submit(Long userId, Long assessmentId, AssessmentSubmitRequest request) {
        AssessmentEntity entity = requireOwnedAssessment(userId, assessmentId);
        if (entity.getStatus() != AssessmentStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "assessment has already been submitted");
        }

        List<String> answers = request.getAnswers();
        BigDecimal score = calculateScore(answers);
        List<String> suggestions = buildSuggestions(score);

        entity.setAnswersJson(toJson(answers));
        entity.setResultJson(toJson(Map.of(
                "score", score,
                "suggestions", suggestions
        )));
        entity.setAdjustmentSuggestionsJson(toJson(suggestions));
        entity.setStatus(AssessmentStatus.SUBMITTED);
        assessmentMapper.update(entity);
        return toResponse(entity);
    }

    @Override
    public AssessmentResponse get(Long userId, Long assessmentId) {
        return toResponse(requireOwnedAssessment(userId, assessmentId));
    }

    @Override
    public List<AssessmentResponse> history(Long userId, Long planId) {
        userService.loadActiveUser(userId);
        requireOwnedPlan(userId, planId);
        return assessmentMapper.findByPlanId(planId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PlanResponse adjust(Long userId, Long assessmentId, AssessmentAdjustRequest request) {
        AssessmentEntity entity = requireOwnedAssessment(userId, assessmentId);
        if (entity.getStatus() == AssessmentStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "assessment must be submitted before adjustment");
        }

        boolean acceptSuggestions = Boolean.TRUE.equals(request.getAcceptSuggestions());
        if (acceptSuggestions) {
            List<String> suggestions = readStringList(entity.getAdjustmentSuggestionsJson());
            if (!CollectionUtils.isEmpty(suggestions)) {
                PlanAdjustRequest adjustRequest = new PlanAdjustRequest();
                adjustRequest.setSummary("计划已根据评估结果动态调整，重点关注以下方向：" + String.join("；", suggestions));

                List<PlanTaskAdjustRequest> adjustments = planTaskMapper.findByPlanId(entity.getPlanId()).stream()
                        .limit(Math.min(2, suggestions.size()))
                        .map(task -> {
                            PlanTaskAdjustRequest taskAdjustRequest = new PlanTaskAdjustRequest();
                            taskAdjustRequest.setTaskId(task.getId());
                            taskAdjustRequest.setDescription(task.getDescription() + " 调整建议：" + suggestions.get(Math.min(task.getSortOrder() - 1, suggestions.size() - 1)));
                            return taskAdjustRequest;
                        })
                        .toList();
                adjustRequest.setAdjustments(adjustments);

                entity.setStatus(AssessmentStatus.ADJUSTED);
                assessmentMapper.update(entity);
                return planService.update(userId, entity.getPlanId(), adjustRequest);
            }
        }

        entity.setStatus(AssessmentStatus.ADJUSTED);
        assessmentMapper.update(entity);
        return planService.get(userId, entity.getPlanId());
    }

    private PlanEntity requireOwnedPlan(Long userId, Long planId) {
        PlanEntity plan = planMapper.findById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "plan not found");
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "plan does not belong to current user");
        }
        return plan;
    }

    private AssessmentEntity requireOwnedAssessment(Long userId, Long assessmentId) {
        userService.loadActiveUser(userId);
        AssessmentEntity entity = assessmentMapper.findById(assessmentId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "assessment not found");
        }
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "assessment does not belong to current user");
        }
        return entity;
    }

    private AssessmentType parseType(String type) {
        return switch (type.toLowerCase()) {
            case "monthly" -> AssessmentType.MONTHLY;
            case "quarterly" -> AssessmentType.QUARTERLY;
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST, "unsupported assessment type");
        };
    }

    private List<AssessmentQuestionResponse> buildQuestions(AssessmentType type) {
        List<AssessmentQuestionResponse> questions = new ArrayList<>();
        questions.add(new AssessmentQuestionResponse(1, "过去一段时间里，你的计划执行完成度如何？"));
        questions.add(new AssessmentQuestionResponse(2, "你在技能提升方面是否有明确进展和成果？"));
        questions.add(new AssessmentQuestionResponse(3, "你当前是否遇到了影响计划推进的阻碍？"));
        if (type == AssessmentType.QUARTERLY) {
            questions.add(new AssessmentQuestionResponse(4, "过去一个季度里，你对目标岗位的理解是否更清晰？"));
        }
        return questions;
    }

    private BigDecimal calculateScore(List<String> answers) {
        int nonEmptyCount = (int) answers.stream().filter(answer -> answer != null && !answer.trim().isEmpty()).count();
        return BigDecimal.valueOf(nonEmptyCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(Math.max(answers.size(), 1)), 2, RoundingMode.HALF_UP);
    }

    private List<String> buildSuggestions(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return List.of("继续保持当前节奏，增加项目成果沉淀", "开始进入求职表达与面试准备阶段");
        }
        if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return List.of("聚焦一到两个关键技能，避免目标过散", "为当前计划增加固定复盘时间和里程碑检查");
        }
        return List.of("缩小阶段目标范围，先完成最关键的一项能力补齐", "适当延长任务周期，降低单阶段负担");
    }

    private AssessmentResponse toResponse(AssessmentEntity entity) {
        return new AssessmentResponse(
                entity.getId(),
                entity.getPlanId(),
                entity.getAssessmentType().name(),
                entity.getStatus().name(),
                readQuestions(entity.getQuestionsJson()),
                readStringList(entity.getAnswersJson()),
                readResult(entity.getResultJson()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private List<AssessmentQuestionResponse> readQuestions(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            List<AssessmentQuestionResponse> questions = objectMapper.readValue(json, QUESTION_TYPE);
            return questions == null ? Collections.emptyList() : questions;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse assessment questions");
        }
    }

    private List<String> readStringList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse assessment string list");
        }
    }

    private AssessmentResultResponse readResult(String json) {
        if (json == null) {
            return null;
        }
        try {
            Map<String, Object> result = objectMapper.readValue(json, MAP_TYPE);
            BigDecimal score = new BigDecimal(String.valueOf(result.get("score")));
            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            return new AssessmentResultResponse(score, suggestions == null ? Collections.emptyList() : suggestions);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse assessment result");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize assessment data");
        }
    }
}
