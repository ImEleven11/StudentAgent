package com.uestc.studentagent.backend.assessment.mapper;

import com.uestc.studentagent.backend.assessment.entity.AssessmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentMapper {

    AssessmentEntity findById(@Param("id") Long id);

    List<AssessmentEntity> findByPlanId(@Param("planId") Long planId);

    int insert(AssessmentEntity entity);

    int update(AssessmentEntity entity);
}
