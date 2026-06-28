package com.lxd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("SELECT r.role_code FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(Long userId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(Long userId, Long roleId);
}