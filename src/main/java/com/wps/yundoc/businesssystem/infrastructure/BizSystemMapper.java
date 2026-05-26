package com.wps.yundoc.businesssystem.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizSystemMapper {

    int insert(BizSystemPO bizSystem);

    BizSystemPO selectByBusinessSystemId(@Param("businessSystemId") String businessSystemId);

    BizSystemPO selectByClientId(@Param("clientId") String clientId);

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
