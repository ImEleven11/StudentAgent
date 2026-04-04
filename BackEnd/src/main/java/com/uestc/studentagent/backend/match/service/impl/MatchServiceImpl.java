package com.uestc.studentagent.backend.match.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.match.dto.MatchAnalysisResponse;
import com.uestc.studentagent.backend.match.dto.MatchAnalyzeRequest;
import com.uestc.studentagent.backend.match.dto.MatchHistoryItemResponse;
import com.uestc.studentagent.backend.match.dto.MatchRecommendRequest;
import com.uestc.studentagent.backend.match.dto.MatchRecommendationItemResponse;
import com.uestc.studentagent.backend.match.entity.MatchRecordEntity;
import com.uestc.studentagent.backend.match.mapper.MatchRecordMapper;
import com.uestc.studentagent.backend.match.service.MatchService;
import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import com.uestc.studentagent.backend.profile.mapper.StudentProfileMapper;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final MatchRecordMapper matchRecordMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final JobMapper jobMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public MatchServiceImpl(MatchRecordMapper matchRecordMapper,
                            StudentProfileMapper studentProfileMapper,
                            JobMapper jobMapper,
                            UserService userService,
                            ObjectMapper objectMapper) {
        this.matchRecordMapper = matchRecordMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.jobMapper = jobMapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public MatchAnalysisResponse analyze(Long userId, MatchAnalyzeRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = requireOwnedProfile(userId, request.getProfileId());
        JobSelection selection = resolveJob(profile, request.getJobId());
        JobEntity job = selection.job();
        ScoreBundle scoreBundle = selection.scoreBundle();
        MatchRecordEntity record = new MatchRecordEntity();
        record.setUserId(userId);
        record.setProfileId(profile.getId());
        record.setJobId(job.getId());
        record.setOverallScore(scoreBundle.overallScore());
        record.setBasicScore(scoreBundle.basicScore());
        record.setSkillScore(scoreBundle.skillScore());
        record.setPotentialScore(scoreBundle.potentialScore());
        record.setDetailsJson(toJson(Map.of(
                "matchedSkills", scoreBundle.matchedSkills(),
                "missingSkills", scoreBundle.missingSkills(),
                "summary", scoreBundle.summary()
        )));
        matchRecordMapper.insert(record);

        return new MatchAnalysisResponse(
                profile.getId(),
                job.getId(),
                job.getTitle(),
                scoreBundle.overallScore(),
                scoreBundle.basicScore(),
                scoreBundle.skillScore(),
                scoreBundle.potentialScore(),
                scoreBundle.matchedSkills(),
                scoreBundle.missingSkills(),
                scoreBundle.summary(),
                record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt()
        );
    }

    @Override
    public List<MatchRecommendationItemResponse> recommend(Long userId, MatchRecommendRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = requireOwnedProfile(userId, request.getProfileId());
        int topN = request.getTopN() == null ? 10 : Math.min(request.getTopN(), 50);

        return jobMapper.findAll().stream()
                .map(job -> toRecommendation(job, calculateScore(profile, job)))
                .sorted(Comparator.comparing(MatchRecommendationItemResponse::matchScore).reversed())
                .limit(topN)
                .toList();
    }

    @Override
    public List<MatchHistoryItemResponse> history(Long userId, Long profileId) {
        userService.loadActiveUser(userId);
        requireOwnedProfile(userId, profileId);

        return matchRecordMapper.findByUserAndProfileId(userId, profileId).stream()
                .map(record -> new MatchHistoryItemResponse(
                        record.getId(),
                        record.getProfileId(),
                        record.getJobId(),
                        record.getOverallScore(),
                        record.getBasicScore(),
                        record.getSkillScore(),
                        record.getPotentialScore(),
                        record.getCreatedAt()
                ))
                .toList();
    }

    private StudentProfileEntity requireOwnedProfile(Long userId, Long profileId) {
        StudentProfileEntity profile = studentProfileMapper.findById(profileId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "profile not found");
        }
        if (!profile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "profile does not belong to current user");
        }
        return profile;
    }

    private JobSelection resolveJob(StudentProfileEntity profile, Long jobId) {
        if (jobId != null) {
            JobEntity job = jobMapper.findById(jobId);
            if (job == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "job not found");
            }
            return new JobSelection(job, calculateScore(profile, job));
        }
        List<JobEntity> allJobs = jobMapper.findAll();
        if (allJobs.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "no jobs available");
        }
        return allJobs.stream()
                .map(job -> new JobSelection(job, calculateScore(profile, job)))
                .max(Comparator.comparing(selection -> selection.scoreBundle().overallScore()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "no jobs available"));
    }

    private MatchRecommendationItemResponse toRecommendation(JobEntity job, ScoreBundle bundle) {
        return new MatchRecommendationItemResponse(
                job.getId(),
                job.getTitle(),
                job.getIndustry(),
                job.getLocation(),
                bundle.overallScore(),
                bundle.matchedSkills(),
                bundle.missingSkills()
        );
    }

    private ScoreBundle calculateScore(StudentProfileEntity profile, JobEntity job) {
        List<String> profileSkills = readStringList(profile.getSkillsJson());
        List<String> jobSkills = readStringList(job.getSkillsJson());

        Set<String> normalizedProfileSkills = profileSkills.stream()
                .filter(StringUtils::hasText)
                .map(skill -> skill.trim().toLowerCase())
                .collect(Collectors.toSet());
        Set<String> normalizedJobSkills = jobSkills.stream()
                .filter(StringUtils::hasText)
                .map(skill -> skill.trim().toLowerCase())
                .collect(Collectors.toSet());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        for (String jobSkill : jobSkills) {
            if (normalizedProfileSkills.contains(jobSkill.trim().toLowerCase())) {
                matchedSkills.add(jobSkill);
            } else {
                missingSkills.add(jobSkill);
            }
        }

        BigDecimal completeness = profile.getCompletenessScore() == null ? BigDecimal.ZERO : profile.getCompletenessScore();
        BigDecimal basicScore = completeness.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal skillScore = normalizedJobSkills.isEmpty()
                ? BigDecimal.valueOf(60).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(matchedSkills.size())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(normalizedJobSkills.size()), 2, RoundingMode.HALF_UP);
        BigDecimal potentialScore = (profile.getAbilityScore() == null ? BigDecimal.ZERO : profile.getAbilityScore())
                .multiply(BigDecimal.valueOf(0.85))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal overallScore = basicScore.multiply(BigDecimal.valueOf(0.25))
                .add(skillScore.multiply(BigDecimal.valueOf(0.45)))
                .add(potentialScore.multiply(BigDecimal.valueOf(0.30)))
                .setScale(2, RoundingMode.HALF_UP);

        String summary = missingSkills.isEmpty()
                ? "当前画像与岗位要求较为贴合，可以优先进入计划生成和资源推荐阶段。"
                : "当前画像已具备部分岗位要求，建议优先补足缺失技能：" + String.join("、", missingSkills);

        return new ScoreBundle(overallScore, basicScore, skillScore, potentialScore, matchedSkills, missingSkills, summary);
    }

    private List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse json list");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize match details");
        }
    }

    private record ScoreBundle(
            BigDecimal overallScore,
            BigDecimal basicScore,
            BigDecimal skillScore,
            BigDecimal potentialScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String summary
    ) {
    }

    private record JobSelection(JobEntity job, ScoreBundle scoreBundle) {
    }
}
