package com.wps.yundoc.businesssystem.application;

import com.wps.yundoc.businesssystem.api.BusinessSystemResponse;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemApiPermissionPO;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessSystemAssembler {

    public BusinessSystemResponse toResponse(
            BizSystemPO bizSystem,
            List<BizSystemApiPermissionPO> permissions) {
        BusinessSystemResponse response = new BusinessSystemResponse();
        fillProfile(response, bizSystem);
        fillVersions(response, bizSystem);
        response.setApiPermissions(apiCodes(permissions));
        return response;
    }

    private void fillProfile(BusinessSystemResponse response, BizSystemPO bizSystem) {
        response.setBusinessSystemId(bizSystem.getBusinessSystemId());
        response.setBusinessSystemName(bizSystem.getBusinessSystemName());
        response.setClientId(bizSystem.getClientId());
        response.setStatus(bizSystem.getStatus());
        response.setDescription(bizSystem.getDescription());
    }

    private void fillVersions(BusinessSystemResponse response, BizSystemPO bizSystem) {
        response.setTokenVersion(bizSystem.getTokenVersion());
        response.setPermissionVersion(bizSystem.getPermissionVersion());
        response.setJwtTtlSeconds(bizSystem.getJwtTtlSeconds());
        response.setCreatedAt(bizSystem.getCreatedAt());
        response.setUpdatedAt(bizSystem.getUpdatedAt());
    }

    private List<String> apiCodes(List<BizSystemApiPermissionPO> permissions) {
        return permissions.stream()
                .map(BizSystemApiPermissionPO::getApiCode)
                .collect(Collectors.toList());
    }
}
