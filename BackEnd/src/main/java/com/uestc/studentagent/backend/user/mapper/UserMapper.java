package com.uestc.studentagent.backend.user.mapper;

import com.uestc.studentagent.backend.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserEntity findById(@Param("id") Long id);

    UserEntity findByUsername(@Param("username") String username);

    UserEntity findByEmail(@Param("email") String email);

    UserEntity findByAccount(@Param("account") String account);

    int insert(UserEntity user);

    int updateProfile(UserEntity user);
}
