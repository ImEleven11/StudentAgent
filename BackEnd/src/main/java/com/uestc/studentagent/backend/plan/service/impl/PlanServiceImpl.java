package com.uestc.studentagent.backend.plan.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.plan.dto.PlanAdjustRequest;
import com.uestc.studentagent.backend.plan.dto.PlanGenerateRequest;
import com.uestc.studentagent.backend.plan.dto.PlanProgressResponse;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;
import com.uestc.studentagent.backend.plan.dto.PlanTaskAdjustRequest;
import com.uestc.studentagent.backend.plan.dto.PlanTaskResponse;
import com.uestc.studentagent.backend.plan.entity.PlanEntity;
import com.uestc.studentagent.backend.plan.entity.PlanStatus;
import com.uestc.studentagent.backend.plan.entity.PlanTaskEntity;
import com.uestc.studentagent.backend.plan.mapper.PlanMapper;
import com.uestc.studentagent.backend.plan.mapper.PlanTaskMapper;
import com.uestc.studentagent.backend.plan.service.PlanService;
import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import com.uestc.studentagent.backend.profile.mapper.StudentProfileMapper;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final PlanMapper planMapper;
    private final PlanTaskMapper planTaskMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final JobMapper jobMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public PlanServiceImpl(PlanMapper planMapper,
                           PlanTaskMapper planTaskMapper,
                           StudentProfileMapper studentProfileMapper,
                           JobMapper jobMapper,
                           UserService userService,
                           ObjectMapper objectMapper) {
        this.planMapper = planMapper;
        this.planTaskMapper = planTaskMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.jobMapper = jobMapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public PlanResponse generate(Long userId, PlanGenerateRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = requireOwnedProfile(userId, request.getProfileId());
        JobEntity targetJob = resolveTargetJob(profile, request.getTargetJobId());

        PlanEntity plan = new PlanEntity();
        plan.setUserId(userId);
        plan.setProfileId(profile.getId());
        plan.setTargetJobId(targetJob == null ? null : targetJob.getId());
        plan.setTitle(targetJob == null ? "职业成长计划" : targetJob.getTitle() + " 阶段化成长计划");
        plan.setSummary(buildPlanSummary(profile, targetJob));
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setProgress(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        planMapper.insert(plan);

        List<PlanTaskEntity> tasks = generateTasks(plan.getId(), profile, targetJob);
        tasks.forEach(planTaskMapper::insert);
        refreshPlanProgress(plan);

        return toPlanResponse(plan);
    }

    @Override
    public PlanResponse get(Long userId, Long planId) {
        return toPlanResponse(requireOwnedPlan(userId, planId));
    }

    @Override
    @Transactional
    public PlanResponse update(Long userId, Long planId, PlanAdjustRequest request) {
        PlanEntity plan = requireOwnedPlan(userId, planId);
        if (StringUtils.hasText(request.getTitle())) {
            plan.setTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getSummary())) {
            plan.setSummary(request.getSummary().trim());
        }
        if (!CollectionUtils.isEmpty(request.getAdjustments())) {
            for (PlanTaskAdjustRequest adjustment : request.getAdjustments()) {
                PlanTaskEntity task = requireOwnedTask(planId, adjustment.getTaskId());
                if (StringUtils.hasText(adjustment.getPhaseName())) {
                    task.setPhaseName(adjustment.getPhaseName().trim());
                }
                if (StringUtils.hasText(adjustment.getTaskTitle())) {
                    task.setTaskTitle(adjustment.getTaskTitle().trim());
                }
                if (StringUtils.hasText(adjustment.getDescription())) {
                    task.setDescription(adjustment.getDescription().trim());
                }
                if (adjustment.getRecommendedDays() != null && adjustment.getRecommendedDays() > 0) {
                    task.setRecommendedDays(adjustment.getRecommendedDays());
                }
                if (adjustment.getCompleted() != null) {
                    task.setCompleted(adjustment.getCompleted());
                }
                planTaskMapper.update(task);
            }
        }
        refreshPlanProgress(plan);
        return toPlanResponse(plan);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long planId) {
        PlanEntity plan = requireOwnedPlan(userId, planId);
        planTaskMapper.deleteByPlanId(plan.getId());
        planMapper.deleteById(plan.getId());
    }

    @Override
    public List<PlanResponse> list(Long userId, Long profileId) {
        userService.loadActiveUser(userId);
        requireOwnedProfile(userId, profileId);
        return planMapper.findByUserAndProfileId(userId, profileId).stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Override
    public PlanProgressResponse progress(Long userId, Long planId) {
        PlanEntity plan = requireOwnedPlan(userId, planId);
        List<PlanTaskResponse> completedTasks = planTaskMapper.findByPlanId(planId).stream()
                .filter(task -> Boolean.TRUE.equals(task.getCompleted()))
                .map(PlanTaskResponse::from)
                .toList();
        return new PlanProgressResponse(plan.getId(), plan.getProgress(), completedTasks);
    }

    @Override
    @Transactional
    public PlanResponse completeTask(Long userId, Long planId, Long taskId) {
        PlanEntity plan = requireOwnedPlan(userId, planId);
        PlanTaskEntity task = requireOwnedTask(planId, taskId);
        task.setCompleted(true);
        planTaskMapper.update(task);
        refreshPlanProgress(plan);
        return toPlanResponse(plan);
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

    private PlanEntity requireOwnedPlan(Long userId, Long planId) {
        userService.loadActiveUser(userId);
        PlanEntity plan = planMapper.findById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "plan not found");
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "plan does not belong to current user");
        }
        return plan;
    }

    private PlanTaskEntity requireOwnedTask(Long planId, Long taskId) {
        PlanTaskEntity task = planTaskMapper.findById(taskId);
        if (task == null || !task.getPlanId().equals(planId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "task not found");
        }
        return task;
    }

    private JobEntity resolveTargetJob(StudentProfileEntity profile, Long targetJobId) {
        if (targetJobId != null) {
            JobEntity job = jobMapper.findById(targetJobId);
            if (job == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "target job not found");
            }
            return job;
        }
        return jobMapper.findAll().stream()
                .max(Comparator.comparing(job -> overlapScore(readList(profile.getSkillsJson()), readList(job.getSkillsJson()))))
                .orElse(null);
    }

    private List<PlanTaskEntity> generateTasks(Long planId, StudentProfileEntity profile, JobEntity targetJob) {
        List<PlanTaskEntity> tasks = new ArrayList<>();
        List<String> profileSkills = readList(profile.getSkillsJson());
        List<String> targetSkills = targetJob == null ? Collections.emptyList() : readList(targetJob.getSkillsJson());

        List<String> missingSkills = targetSkills.stream()
                .filter(skill -> profileSkills.stream().noneMatch(existing -> existing.equalsIgnoreCase(skill)))
                .toList();

        int sortOrder = 1;
        tasks.add(buildTask(planId, "阶段一：画像补全", "完善个人画像", "补充教育背景、兴趣、性格与项目经历，提升画像完整度。", 5, false, sortOrder++));

        for (String missingSkill : missingSkills.stream().limit(3).toList()) {
            tasks.add(buildTask(
                    planId,
                    "阶段二：能力补齐",
                    "学习技能：" + missingSkill,
                    "围绕目标岗位补齐关键技能 " + missingSkill + "，完成课程学习和练习任务。",
                    10,
                    false,
                    sortOrder++
            ));
        }

        tasks.add(buildTask(planId, "阶段三：项目实践", "完成一个目标岗位相关项目", "将已学习技能落到项目实践中，形成可展示成果。", 14, false, sortOrder++));
        tasks.add(buildTask(planId, "阶段四：求职准备", "完善简历并进行岗位投递", "针对目标岗位优化简历、项目描述并准备面试。", 7, false, sortOrder));
        return tasks;
    }

    private PlanTaskEntity buildTask(Long planId,
                                     String phaseName,
                                     String taskTitle,
                                     String description,
                                     int recommendedDays,
                                     boolean completed,
                                     int sortOrder) {
        PlanTaskEntity task = new PlanTaskEntity();
        task.setPlanId(planId);
        task.setPhaseName(phaseName);
        task.setTaskTitle(taskTitle);
        task.setDescription(description);
        task.setRecommendedDays(recommendedDays);
        task.setCompleted(completed);
        task.setSortOrder(sortOrder);
        return task;
    }

    private void refreshPlanProgress(PlanEntity plan) {
        List<PlanTaskEntity> tasks = planTaskMapper.findByPlanId(plan.getId());
        long total = tasks.size();
        long completed = tasks.stream().filter(task -> Boolean.TRUE.equals(task.getCompleted())).count();
        BigDecimal progress = total == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        plan.setProgress(progress);
        plan.setStatus(completed == total && total > 0 ? PlanStatus.COMPLETED : PlanStatus.ACTIVE);
        planMapper.update(plan);
    }

    private PlanResponse toPlanResponse(PlanEntity plan) {
        List<PlanTaskResponse> tasks = planTaskMapper.findByPlanId(plan.getId()).stream()
                .map(PlanTaskResponse::from)
                .toList();
        return PlanResponse.from(plan, tasks);
    }

    private String buildPlanSummary(StudentProfileEntity profile, JobEntity targetJob) {
        if (targetJob == null) {
            return "基于当前画像生成的通用成长计划，建议先补全画像并逐步强化项目实践。";
        }
        List<String> missingSkills = readList(targetJob.getSkillsJson()).stream()
                .filter(skill -> readList(profile.getSkillsJson()).stream().noneMatch(existing -> existing.equalsIgnoreCase(skill)))
                .toList();
        return missingSkills.isEmpty()
                ? "你的当前画像与目标岗位较为接近，计划重点放在项目实践、简历优化和求职准备。"
                : "围绕目标岗位 " + targetJob.getTitle() + "，当前计划重点补齐技能：" + String.join("、", missingSkills);
    }

    private int overlapScore(List<String> profileSkills, List<String> targetSkills) {
        if (CollectionUtils.isEmpty(targetSkills)) {
            return 0;
        }
        int count = 0;
        for (String target : targetSkills) {
            boolean matched = profileSkills.stream().anyMatch(existing -> existing.equalsIgnoreCase(target));
            if (matched) {
                count++;
            }
        }
        return count;
    }

    private List<String> readList(String json) {
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
}
