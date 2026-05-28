package com.wps.yundoc.adminuser.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminUserMapper {

    int insert(AdminUserPO adminUser);

    AdminUserPO selectByUsername(@Param("username") String username);

    List<AdminUserPO> selectPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("role") String role,
            @Param("limit") int limit,
            @Param("offset") int offset);

    int updateProfile(
            @Param("username") String username,
            @Param("displayName") String displayName,
            @Param("role") String role,
            @Param("status") String status,
            @Param("updatedAt") LocalDateTime updatedAt);

    int updateLastLoginAt(
            @Param("username") String username,
            @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
