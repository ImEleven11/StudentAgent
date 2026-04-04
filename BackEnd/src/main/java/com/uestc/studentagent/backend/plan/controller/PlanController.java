package com.uestc.studentagent.backend.plan.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.plan.dto.PlanAdjustRequest;
import com.uestc.studentagent.backend.plan.dto.PlanGenerateRequest;
import com.uestc.studentagent.backend.plan.dto.PlanProgressResponse;
import com.uestc.studentagent.backend.plan.dto.PlanResponse;
import com.uestc.studentagent.backend.plan.service.PlanService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
@Tag(name = "Plan")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a staged plan")
    public ApiResponse<PlanResponse> generate(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                              @Valid @RequestBody PlanGenerateRequest request) {
        return ApiResponse.success(planService.generate(principal.getUserId(), request));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get plan detail")
    public ApiResponse<PlanResponse> get(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                         @PathVariable Long planId) {
        return ApiResponse.success(planService.get(principal.getUserId(), planId));
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Adjust a plan")
    public ApiResponse<PlanResponse> update(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                            @PathVariable Long planId,
                                            @Valid @RequestBody PlanAdjustRequest request) {
        return ApiResponse.success(planService.update(principal.getUserId(), planId, request));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete a plan")
    public ApiResponse<Void> delete(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                    @PathVariable Long planId) {
        planService.delete(principal.getUserId(), planId);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @Operation(summary = "List plans for a profile")
    public ApiResponse<List<PlanResponse>> list(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                @RequestParam Long profileId) {
        return ApiResponse.success(planService.list(principal.getUserId(), profileId));
    }

    @GetMapping("/{planId}/progress")
    @Operation(summary = "Get plan progress")
    public ApiResponse<PlanProgressResponse> progress(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                      @PathVariable Long planId) {
        return ApiResponse.success(planService.progress(principal.getUserId(), planId));
    }

    @PostMapping("/{planId}/task/{taskId}/complete")
    @Operation(summary = "Mark a task as completed")
    public ApiResponse<PlanResponse> completeTask(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                  @PathVariable Long planId,
                                                  @PathVariable Long taskId) {
        return ApiResponse.success(planService.completeTask(principal.getUserId(), planId, taskId));
    }
}
