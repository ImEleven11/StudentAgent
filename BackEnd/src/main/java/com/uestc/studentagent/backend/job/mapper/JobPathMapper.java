package com.uestc.studentagent.backend.job.mapper;

import com.uestc.studentagent.backend.job.entity.JobPathEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobPathMapper {

    List<JobPathEntity> findByJobId(@Param("jobId") Long jobId);
}
