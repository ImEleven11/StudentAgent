package com.uestc.studentagent.backend.match.service;

import com.uestc.studentagent.backend.match.dto.MatchAnalysisResponse;
import com.uestc.studentagent.backend.match.dto.MatchAnalyzeRequest;
import com.uestc.studentagent.backend.match.dto.MatchHistoryItemResponse;
import com.uestc.studentagent.backend.match.dto.MatchRecommendRequest;
import com.uestc.studentagent.backend.match.dto.MatchRecommendationItemResponse;

import java.util.List;

public interface MatchService {

    MatchAnalysisResponse analyze(Long userId, MatchAnalyzeRequest request);

    List<MatchRecommendationItemResponse> recommend(Long userId, MatchRecommendRequest request);

    List<MatchHistoryItemResponse> history(Long userId, Long profileId);
}
