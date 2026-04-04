package com.uestc.studentagent.backend.job.mapper;

import com.uestc.studentagent.backend.job.entity.JobEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobMapper {

    JobEntity findById(@Param("id") Long id);

    List<JobEntity> search(@Param("keyword") String keyword,
                           @Param("industry") String industry,
                           @Param("location") String location,
                           @Param("offset") int offset,
                           @Param("size") int size);

    long countSearch(@Param("keyword") String keyword,
                     @Param("industry") String industry,
                     @Param("location") String location);

    List<JobEntity> findAll();

    int insert(JobEntity entity);
}
