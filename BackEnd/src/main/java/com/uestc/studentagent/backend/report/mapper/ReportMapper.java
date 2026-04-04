package com.uestc.studentagent.backend.report.mapper;

import com.uestc.studentagent.backend.report.entity.ReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    ReportEntity findById(@Param("id") Long id);

    List<ReportEntity> findByUserAndProfileId(@Param("userId") Long userId, @Param("profileId") Long profileId);

    int insert(ReportEntity entity);

    int update(ReportEntity entity);
}
