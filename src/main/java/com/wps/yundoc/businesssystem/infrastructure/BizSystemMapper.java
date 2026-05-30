package com.wps.yundoc.businesssystem.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizSystemMapper {

    String PARAM_BUSINESS_SYSTEM_ID = "businessSystemId";
    String PARAM_UPDATED_AT = "updatedAt";

    int insert(BizSystemPO bizSystem);

    BizSystemPO selectByBusinessSystemId(@Param(PARAM_BUSINESS_SYSTEM_ID) String businessSystemId);

    BizSystemPO selectByClientId(@Param("clientId") String clientId);

    java.util.List<BizSystemPO> selectPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    int updateProfile(
            @Param(PARAM_BUSINESS_SYSTEM_ID) String businessSystemId,
            @Param("businessSystemName") String businessSystemName,
            @Param("status") String status,
            @Param("jwtTtlSeconds") Integer jwtTtlSeconds,
            @Param("description") String description,
            @Param(PARAM_UPDATED_AT) java.time.LocalDateTime updatedAt);

    int updateSecret(
            @Param(PARAM_BUSINESS_SYSTEM_ID) String businessSystemId,
            @Param("clientSecretDigest") String clientSecretDigest,
            @Param("clientSecretSalt") String clientSecretSalt,
            @Param("clientSecretAlg") String clientSecretAlg,
            @Param(PARAM_UPDATED_AT) java.time.LocalDateTime updatedAt);

    int increasePermissionVersion(
            @Param(PARAM_BUSINESS_SYSTEM_ID) String businessSystemId,
            @Param(PARAM_UPDATED_AT) java.time.LocalDateTime updatedAt);
}
