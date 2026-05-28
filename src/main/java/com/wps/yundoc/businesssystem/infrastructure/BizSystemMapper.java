package com.wps.yundoc.businesssystem.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizSystemMapper {

    int insert(BizSystemPO bizSystem);

    BizSystemPO selectByBusinessSystemId(@Param("businessSystemId") String businessSystemId);

    BizSystemPO selectByClientId(@Param("clientId") String clientId);

    java.util.List<BizSystemPO> selectPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    int updateProfile(
            @Param("businessSystemId") String businessSystemId,
            @Param("businessSystemName") String businessSystemName,
            @Param("status") String status,
            @Param("jwtTtlSeconds") Integer jwtTtlSeconds,
            @Param("description") String description,
            @Param("updatedAt") java.time.LocalDateTime updatedAt);

    int updateSecret(
            @Param("businessSystemId") String businessSystemId,
            @Param("clientSecretDigest") String clientSecretDigest,
            @Param("clientSecretSalt") String clientSecretSalt,
            @Param("clientSecretAlg") String clientSecretAlg,
            @Param("updatedAt") java.time.LocalDateTime updatedAt);

    int increasePermissionVersion(
            @Param("businessSystemId") String businessSystemId,
            @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
