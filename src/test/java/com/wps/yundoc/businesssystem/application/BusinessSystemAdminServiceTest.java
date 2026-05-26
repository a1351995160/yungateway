package com.wps.yundoc.businesssystem.application;

import com.wps.yundoc.auth.application.ClientSecretDigestService;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemSecretResponse;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemMapper;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemPO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessSystemAdminServiceTest {

    @Autowired
    private BusinessSystemAdminService adminService;

    @Autowired
    private BizSystemMapper bizSystemMapper;

    @Autowired
    private ClientSecretDigestService digestService;

    @Test
    void resetSecretInvalidatesPreviousDigestAndIncreasesTokenVersion() {
        BusinessSystemCreateResponse created = adminService.create(createRequest("biz-reset"));
        BizSystemPO before = bizSystemMapper.selectByBusinessSystemId("biz-reset");

        BusinessSystemSecretResponse reset = adminService.resetClientSecret("biz-reset");
        BizSystemPO after = bizSystemMapper.selectByBusinessSystemId("biz-reset");

        assertThat(reset.getTokenVersion()).isEqualTo(before.getTokenVersion() + 1);
        assertThat(after.getClientSecretDigest()).isNotEqualTo(before.getClientSecretDigest());
        assertThat(matches(created.getClientSecret(), after)).isFalse();
        assertThat(matches(reset.getClientSecret(), after)).isTrue();
    }

    @Test
    void savePermissionsIncreasesPermissionVersion() {
        adminService.create(createRequest("biz-permission"));
        BizSystemPO before = bizSystemMapper.selectByBusinessSystemId("biz-permission");

        adminService.savePermissions("biz-permission", permissionRequest("user-files:list"));
        BizSystemPO after = bizSystemMapper.selectByBusinessSystemId("biz-permission");

        assertThat(after.getPermissionVersion()).isEqualTo(before.getPermissionVersion() + 1);
    }

    private boolean matches(String secret, BizSystemPO bizSystem) {
        return digestService.matches(
                secret,
                bizSystem.getClientSecretSalt(),
                bizSystem.getClientSecretAlg(),
                bizSystem.getClientSecretDigest());
    }

    private BusinessSystemCreateRequest createRequest(String businessSystemId) {
        BusinessSystemCreateRequest request = new BusinessSystemCreateRequest();
        request.setBusinessSystemId(businessSystemId);
        request.setBusinessSystemName("Contract System");
        request.setJwtTtlSeconds(1800);
        return request;
    }

    private BusinessSystemApiPermissionUpdateRequest permissionRequest(String apiCode) {
        BusinessSystemApiPermissionUpdateRequest request = new BusinessSystemApiPermissionUpdateRequest();
        request.setApiPermissions(Collections.singletonList(apiCode));
        return request;
    }
}
