package com.lxd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxd.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT username FROM sys_user WHERE username = #{username}")
    String checkUsernameExists(@Param("username") String username);
}
