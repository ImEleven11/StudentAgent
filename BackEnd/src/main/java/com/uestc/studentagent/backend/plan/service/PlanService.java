package com.uestc.studentagent.backend.plan.service;

import com.uestc.studentagent.backend.plan.dto.PlanAdjustRequest;
import com.uestc.studentagent.backend.plan.dto.PlanGenerateRequest;
import com.uestc.studentagent.backend.plan.dto.PlanProgressResponse;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;

import java.util.List;

public interface PlanService {

    PlanResponse generate(Long userId, PlanGenerateRequest request);

    PlanResponse get(Long userId, Long planId);

    PlanResponse update(Long userId, Long planId, PlanAdjustRequest request);

    void delete(Long userId, Long planId);

    List<PlanResponse> list(Long userId, Long profileId);

    PlanProgressResponse progress(Long userId, Long planId);

    PlanResponse completeTask(Long userId, Long planId, Long taskId);
}
