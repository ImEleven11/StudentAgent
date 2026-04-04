package com.uestc.studentagent.backend.profile.mapper;

import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentProfileMapper {

    StudentProfileEntity findById(@Param("id") Long id);

    StudentProfileEntity findByUserId(@Param("userId") Long userId);

    int insert(StudentProfileEntity profile);

    int update(StudentProfileEntity profile);
}
