package com.uestc.studentagent.backend.resource.mapper;

import com.uestc.studentagent.backend.resource.entity.ResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceMapper {

    List<ResourceEntity> search(@Param("resourceType") String resourceType,
                                @Param("levelTag") String levelTag,
                                @Param("difficultyTag") String difficultyTag,
                                @Param("locationTag") String locationTag,
                                @Param("categoryTag") String categoryTag,
                                @Param("timeRangeTag") String timeRangeTag);

    int insert(ResourceEntity entity);

    int update(ResourceEntity entity);

    ResourceEntity findById(@Param("id") Long id);
}
