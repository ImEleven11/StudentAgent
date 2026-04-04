package com.uestc.studentagent.backend.admin.mapper;

import com.uestc.studentagent.backend.admin.entity.SystemConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemConfigMapper {

    SystemConfigEntity findByKey(@Param("configKey") String configKey);

    int insert(SystemConfigEntity entity);

    int update(SystemConfigEntity entity);
}
