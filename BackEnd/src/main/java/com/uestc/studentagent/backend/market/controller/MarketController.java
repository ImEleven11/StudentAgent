package com.uestc.studentagent.backend.market.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.market.dto.HotJobResponse;
import com.uestc.studentagent.backend.market.dto.MarketForecastRequest;
import com.uestc.studentagent.backend.market.dto.MarketForecastResponse;
import com.uestc.studentagent.backend.market.dto.MarketTrendResponse;
import com.uestc.studentagent.backend.market.dto.SupplyDemandResponse;
import com.uestc.studentagent.backend.market.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@Tag(name = "Market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/supply-demand/{jobId}")
    @Operation(summary = "Get supply-demand index for a job")
    public ApiResponse<SupplyDemandResponse> supplyDemand(@PathVariable Long jobId) {
        return ApiResponse.success(marketService.supplyDemand(jobId));
    }

    @GetMapping("/hot-jobs")
    @Operation(summary = "Get hot jobs")
    public ApiResponse<List<HotJobResponse>> hotJobs(@RequestParam(required = false) String industry,
                                                     @RequestParam(required = false) String city,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(marketService.hotJobs(industry, city, limit));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get industry trend")
    public ApiResponse<MarketTrendResponse> trend(@RequestParam(required = false) String industry) {
        return ApiResponse.success(marketService.trend(industry));
    }

    @PostMapping("/forecast")
    @Operation(summary = "Forecast market demand for a job")
    public ApiResponse<MarketForecastResponse> forecast(@Valid @RequestBody MarketForecastRequest request) {
        return ApiResponse.success(marketService.forecast(request));
    }
}
