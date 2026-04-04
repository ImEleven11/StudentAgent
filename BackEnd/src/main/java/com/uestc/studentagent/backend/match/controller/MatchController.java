package com.uestc.studentagent.backend.match.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.match.dto.MatchAnalysisResponse;
import com.uestc.studentagent.backend.match.dto.MatchAnalyzeRequest;
import com.uestc.studentagent.backend.match.dto.MatchHistoryItemResponse;
import com.uestc.studentagent.backend.match.dto.MatchRecommendRequest;
import com.uestc.studentagent.backend.match.dto.MatchRecommendationItemResponse;
import com.uestc.studentagent.backend.match.service.MatchService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@Tag(name = "Match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze profile-job matching")
    public ApiResponse<MatchAnalysisResponse> analyze(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                      @Valid @RequestBody MatchAnalyzeRequest request) {
        return ApiResponse.success(matchService.analyze(principal.getUserId(), request));
    }

    @PostMapping("/recommend")
    @Operation(summary = "Recommend jobs for a profile")
    public ApiResponse<List<MatchRecommendationItemResponse>> recommend(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                                        @Valid @RequestBody MatchRecommendRequest request) {
        return ApiResponse.success(matchService.recommend(principal.getUserId(), request));
    }

    @GetMapping("/history")
    @Operation(summary = "Get match history for a profile")
    public ApiResponse<List<MatchHistoryItemResponse>> history(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                               @RequestParam Long profileId) {
        return ApiResponse.success(matchService.history(principal.getUserId(), profileId));
    }
}
