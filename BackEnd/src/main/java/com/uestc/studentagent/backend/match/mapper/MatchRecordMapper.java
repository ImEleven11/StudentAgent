package com.uestc.studentagent.backend.match.mapper;

import com.uestc.studentagent.backend.match.entity.MatchRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MatchRecordMapper {

    List<MatchRecordEntity> findByUserAndProfileId(@Param("userId") Long userId, @Param("profileId") Long profileId);

    int insert(MatchRecordEntity entity);
}
