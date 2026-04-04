package com.uestc.studentagent.backend.plan.mapper;

import com.uestc.studentagent.backend.plan.entity.PlanTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlanTaskMapper {

    List<PlanTaskEntity> findByPlanId(@Param("planId") Long planId);

    PlanTaskEntity findById(@Param("id") Long id);

    int insert(PlanTaskEntity entity);

    int update(PlanTaskEntity entity);

    int deleteByPlanId(@Param("planId") Long planId);
}
