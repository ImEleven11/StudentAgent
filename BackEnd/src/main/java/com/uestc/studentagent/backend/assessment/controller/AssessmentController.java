package com.uestc.studentagent.backend.assessment.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentAdjustRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentCreateRequest;
import com.uestc.studentagent.backend.assessment.dto.AssessmentResponse;
import com.uestc.studentagent.backend.assessment.dto.AssessmentSubmitRequest;
import com.uestc.studentagent.backend.assessment.service.AssessmentService;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@Tag(name = "Assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create an assessment")
    public ApiResponse<AssessmentResponse> create(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                  @Valid @RequestBody AssessmentCreateRequest request) {
        return ApiResponse.success(assessmentService.create(principal.getUserId(), request));
    }

    @PostMapping("/{assessmentId}/submit")
    @Operation(summary = "Submit assessment answers")
    public ApiResponse<AssessmentResponse> submit(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                  @PathVariable Long assessmentId,
                                                  @Valid @RequestBody AssessmentSubmitRequest request) {
        return ApiResponse.success(assessmentService.submit(principal.getUserId(), assessmentId, request));
    }

    @GetMapping("/{assessmentId}")
    @Operation(summary = "Get assessment detail")
    public ApiResponse<AssessmentResponse> get(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                               @PathVariable Long assessmentId) {
        return ApiResponse.success(assessmentService.get(principal.getUserId(), assessmentId));
    }

    @GetMapping("/history/{planId}")
    @Operation(summary = "Get assessment history for a plan")
    public ApiResponse<List<AssessmentResponse>> history(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                         @PathVariable Long planId) {
        return ApiResponse.success(assessmentService.history(principal.getUserId(), planId));
    }

    @PostMapping("/{assessmentId}/adjust")
    @Operation(summary = "Adjust plan based on assessment result")
    public ApiResponse<PlanResponse> adjust(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                            @PathVariable Long assessmentId,
                                            @RequestBody AssessmentAdjustRequest request) {
        return ApiResponse.success(assessmentService.adjust(principal.getUserId(), assessmentId, request));
    }
}
