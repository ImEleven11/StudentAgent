package com.uestc.studentagent.backend.market.service;

import com.uestc.studentagent.backend.market.dto.HotJobResponse;
import com.uestc.studentagent.backend.market.dto.MarketForecastRequest;
import com.uestc.studentagent.backend.market.dto.MarketForecastResponse;
import com.uestc.studentagent.backend.market.dto.MarketTrendResponse;
import com.uestc.studentagent.backend.market.dto.SupplyDemandResponse;

import java.util.List;

public interface MarketService {

    SupplyDemandResponse supplyDemand(Long jobId);

    List<HotJobResponse> hotJobs(String industry, String city, Integer limit);

    MarketTrendResponse trend(String industry);

    MarketForecastResponse forecast(MarketForecastRequest request);
}
