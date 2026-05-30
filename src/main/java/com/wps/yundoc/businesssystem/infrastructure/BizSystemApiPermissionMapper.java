package com.wps.yundoc.businesssystem.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizSystemApiPermissionMapper {

    int insert(BizSystemApiPermissionPO permission);

    /**
     * Inserts business-system API permissions in batch.
     *
     * @param permissions permissions to insert
     * @return inserted row count
     */
    int insertAll(@Param("permissions") List<BizSystemApiPermissionPO> permissions);

    BizSystemApiPermissionPO selectByBusinessSystemIdAndApiCode(
            @Param("businessSystemId") String businessSystemId,
            @Param("apiCode") String apiCode);

    List<BizSystemApiPermissionPO> selectByBusinessSystemId(
            @Param("businessSystemId") String businessSystemId);

    /**
     * Lists API permissions for a batch of business systems.
     *
     * @param businessSystemIds business-system ids
     * @return matching permissions
     */
    List<BizSystemApiPermissionPO> selectByBusinessSystemIds(
            @Param("businessSystemIds") List<String> businessSystemIds);

    int deleteByBusinessSystemId(@Param("businessSystemId") String businessSystemId);
}
