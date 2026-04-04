package com.uestc.studentagent.backend.market.mapper;

import com.uestc.studentagent.backend.market.entity.MarketMetricEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketMetricMapper {

    MarketMetricEntity findByJobId(@Param("jobId") Long jobId);

    List<MarketMetricEntity> findHotJobs(@Param("limit") int limit);
}
