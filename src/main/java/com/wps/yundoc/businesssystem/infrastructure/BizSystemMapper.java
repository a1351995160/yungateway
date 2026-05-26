package com.wps.yundoc.businesssystem.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizSystemMapper {

    int insert(BizSystemPO bizSystem);

    BizSystemPO selectByBusinessSystemId(@Param("businessSystemId") String businessSystemId);

    BizSystemPO selectByClientId(@Param("clientId") String clientId);
}
