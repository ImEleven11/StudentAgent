package com.uestc.studentagent.backend.market.service.impl;

import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.market.dto.HotJobResponse;
import com.uestc.studentagent.backend.market.dto.MarketForecastRequest;
import com.uestc.studentagent.backend.market.dto.MarketForecastResponse;
import com.uestc.studentagent.backend.market.dto.MarketTrendResponse;
import com.uestc.studentagent.backend.market.dto.SupplyDemandResponse;
import com.uestc.studentagent.backend.market.entity.MarketMetricEntity;
import com.uestc.studentagent.backend.market.mapper.MarketMetricMapper;
import com.uestc.studentagent.backend.market.service.MarketService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MarketServiceImpl implements MarketService {

    private final MarketMetricMapper marketMetricMapper;
    private final JobMapper jobMapper;

    public MarketServiceImpl(MarketMetricMapper marketMetricMapper, JobMapper jobMapper) {
        this.marketMetricMapper = marketMetricMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public SupplyDemandResponse supplyDemand(Long jobId) {
        JobEntity job = requireJob(jobId);
        MarketMetricEntity metric = marketMetricMapper.findByJobId(jobId);
        if (metric == null) {
            BigDecimal fallback = buildFallbackIndex(job);
            return new SupplyDemandResponse(jobId, fallback, level(fallback), List.of(
                    fallback.subtract(BigDecimal.valueOf(8)),
                    fallback.subtract(BigDecimal.valueOf(3)),
                    fallback
            ));
        }
        return new SupplyDemandResponse(jobId, metric.getSupplyDemandIndex(), metric.getLevelTag(), List.of(
                metric.getSupplyDemandIndex().subtract(BigDecimal.valueOf(8)),
                metric.getSupplyDemandIndex().subtract(BigDecimal.valueOf(3)),
                metric.getSupplyDemandIndex()
        ));
    }

    @Override
    public List<HotJobResponse> hotJobs(String industry, String city, Integer limit) {
        int top = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        List<MarketMetricEntity> metrics = marketMetricMapper.findHotJobs(top * 3);
        return metrics.stream()
                .map(metric -> {
                    JobEntity job = jobMapper.findById(metric.getJobId());
                    return job == null ? null : new HotJobResponse(job.getId(), job.getTitle(), job.getIndustry(), job.getLocation(), metric.getHotScore());
                })
                .filter(response -> response != null)
                .filter(response -> !StringUtils.hasText(industry) || industry.equalsIgnoreCase(response.industry()))
                .filter(response -> !StringUtils.hasText(city) || city.equalsIgnoreCase(response.city()))
                .limit(top)
                .toList();
    }

    @Override
    public MarketTrendResponse trend(String industry) {
        List<JobEntity> jobs = jobMapper.findAll().stream()
                .filter(job -> !StringUtils.hasText(industry) || industry.equalsIgnoreCase(job.getIndustry()))
                .toList();
        if (jobs.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "no industry jobs available");
        }
        String trend = jobs.size() >= 5 ? "上升" : "平稳";
        String salaryTrend = jobs.stream().anyMatch(job -> job.getSalaryMax() != null && job.getSalaryMax().compareTo(BigDecimal.valueOf(20000)) >= 0)
                ? "上涨"
                : "平稳";
        String talentGap = jobs.size() >= 5 ? "中高端技能型人才存在缺口" : "人才供需基本平衡";
        return new MarketTrendResponse(industry == null ? "全部行业" : industry, trend, salaryTrend, talentGap);
    }

    @Override
    public MarketForecastResponse forecast(MarketForecastRequest request) {
        JobEntity job = requireJob(request.getJobId());
        MarketMetricEntity metric = marketMetricMapper.findByJobId(job.getId());
        if (metric != null && request.getPeriod().equalsIgnoreCase(metric.getForecastPeriod())) {
            return new MarketForecastResponse(job.getId(), request.getPeriod(), metric.getForecastResult(), metric.getForecastConfidence());
        }

        BigDecimal base = metric == null ? buildFallbackIndex(job) : metric.getSupplyDemandIndex();
        String result = base.compareTo(BigDecimal.valueOf(70)) >= 0 ? "需求上升" : (base.compareTo(BigDecimal.valueOf(50)) >= 0 ? "平稳" : "下降");
        BigDecimal confidence = base.multiply(BigDecimal.valueOf(0.9)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new MarketForecastResponse(job.getId(), request.getPeriod(), result, confidence);
    }

    private JobEntity requireJob(Long jobId) {
        JobEntity job = jobMapper.findById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "job not found");
        }
        return job;
    }

    private BigDecimal buildFallbackIndex(JobEntity job) {
        BigDecimal base = BigDecimal.valueOf(55);
        if (StringUtils.hasText(job.getIndustry())) {
            base = base.add(BigDecimal.valueOf(5));
        }
        if (job.getSalaryMax() != null && job.getSalaryMax().compareTo(BigDecimal.valueOf(15000)) >= 0) {
            base = base.add(BigDecimal.valueOf(10));
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    private String level(BigDecimal index) {
        if (index.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "高热";
        }
        if (index.compareTo(BigDecimal.valueOf(55)) >= 0) {
            return "中等";
        }
        return "偏低";
    }
}
