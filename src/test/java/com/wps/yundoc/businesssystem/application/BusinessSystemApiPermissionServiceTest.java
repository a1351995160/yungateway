package com.wps.yundoc.businesssystem.application;

import com.wps.yundoc.auth.application.AuthTokenService;
import com.wps.yundoc.auth.domain.BusinessSystemPrincipal;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessSystemApiPermissionServiceTest {

    @Autowired
    private BusinessSystemAdminService adminService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private BusinessSystemApiPermissionService permissionService;

    @Test
    void allowsConfiguredApiPermission() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-permission-allowed");
        adminService.savePermissions("biz-permission-allowed", permissions("app-preview:create"));
        BusinessSystemPrincipal principal = principal(created);

        permissionService.requirePermission(principal, "app-preview:create");
    }

    @Test
    void rejectsMissingApiPermission() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-permission-denied");
        adminService.savePermissions("biz-permission-denied", permissions("user-files:list"));
        BusinessSystemPrincipal principal = principal(created);

        assertThatThrownBy(() -> permissionService.requirePermission(principal, "app-preview:create"))
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.API_PERMISSION_DENIED);
    }

    @Test
    void rejectsStalePermissionVersion() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-permission-stale");
        adminService.savePermissions("biz-permission-stale", permissions("app-preview:create"));
        BusinessSystemPrincipal principal = principal(created);
        adminService.savePermissions("biz-permission-stale", permissions("user-files:list"));

        assertThatThrownBy(() -> permissionService.requirePermission(principal, "app-preview:create"))
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.TOKEN_INVALID);
    }

    @Test
    void rejectsStaleTokenVersion() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-token-stale");
        adminService.savePermissions("biz-token-stale", permissions("app-preview:create"));
        BusinessSystemPrincipal principal = principal(created);
        adminService.resetClientSecret("biz-token-stale");

        assertThatThrownBy(() -> permissionService.requirePermission(principal, "app-preview:create"))
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.TOKEN_INVALID);
    }

    private BusinessSystemPrincipal principal(BusinessSystemCreateResponse created) {
        return authTokenService.issueToken(
                created.getBusinessSystem().getClientId(),
                created.getClientSecret()).getPrincipal();
    }

    private BusinessSystemCreateResponse createBusinessSystem(String businessSystemId) {
        BusinessSystemCreateRequest request = new BusinessSystemCreateRequest();
        request.setBusinessSystemId(businessSystemId);
        request.setBusinessSystemName("Contract System");
        request.setJwtTtlSeconds(1800);
        return adminService.create(request);
    }

    private BusinessSystemApiPermissionUpdateRequest permissions(String apiCode) {
        BusinessSystemApiPermissionUpdateRequest request = new BusinessSystemApiPermissionUpdateRequest();
        request.setApiPermissions(Collections.singletonList(apiCode));
        return request;
    }
}
