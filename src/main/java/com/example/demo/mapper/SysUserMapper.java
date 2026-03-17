package com.example.demo.mapper;


import com.example.demo.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface SysUserMapper {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(String username);

    @Insert("INSERT INTO sys_user (username, password, phone,  create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{phone}, NOW(), NOW())")
    int insert(SysUser user);


    SysUser selectById(Long userId);


    void updateById(SysUser user);
}
