package com.uestc.studentagent.backend.plan.mapper;

import com.uestc.studentagent.backend.plan.entity.PlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlanMapper {

    PlanEntity findById(@Param("id") Long id);

    List<PlanEntity> findByUserAndProfileId(@Param("userId") Long userId, @Param("profileId") Long profileId);

    int insert(PlanEntity entity);

    int update(PlanEntity entity);

    int deleteById(@Param("id") Long id);
}
