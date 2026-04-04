package com.uestc.studentagent.backend.assessment.service;

import com.uestc.studentagent.backend.assessment.dto.AssessmentAdjustRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentCreateRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentSubmitRequest;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;

import java.util.List;

public interface AssessmentService {

    AssessmentResponse create(Long userId, AssessmentCreateRequest request);

    AssessmentResponse submit(Long userId, Long assessmentId, AssessmentSubmitRequest request);

    AssessmentResponse get(Long userId, Long assessmentId);

    List<AssessmentResponse> history(Long userId, Long planId);

    PlanResponse adjust(Long userId, Long assessmentId, AssessmentAdjustRequest request);
}
