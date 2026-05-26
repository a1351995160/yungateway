package com.wps.yundoc.businesssystem.infrastructure;

import com.wps.yundoc.businesssystem.domain.ApiCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BizSystemMapperTest {

    @Autowired
    private BizSystemMapper bizSystemMapper;

    @Autowired
    private BizSystemApiPermissionMapper permissionMapper;

    @Test
    void selectsBusinessSystemByClientId() {
        BizSystemPO bizSystem = newBizSystem("biz-contract", "client-contract");
        bizSystemMapper.insert(bizSystem);

        BizSystemPO selected = bizSystemMapper.selectByClientId("client-contract");

        assertThat(selected).isNotNull();
        assertThat(selected.getBusinessSystemId()).isEqualTo("biz-contract");
        assertThat(selected.getClientSecretDigest()).isEqualTo(repeat("a", 64));
        assertThat(selected.getTokenVersion()).isEqualTo(1);
        assertThat(selected.getPermissionVersion()).isEqualTo(1);
    }

    @Test
    void selectsPermissionByBusinessSystemIdAndApiCode() {
        bizSystemMapper.insert(newBizSystem("biz-file", "client-file"));
        permissionMapper.insert(newPermission("biz-file", ApiCode.USER_FILES_LIST.getCode()));
        permissionMapper.insert(newPermission("biz-file", ApiCode.APP_PREVIEW_CREATE.getCode()));

        BizSystemApiPermissionPO selected = permissionMapper.selectByBusinessSystemIdAndApiCode(
                "biz-file",
                ApiCode.USER_FILES_LIST.getCode());
        List<BizSystemApiPermissionPO> allPermissions = permissionMapper.selectByBusinessSystemId("biz-file");

        assertThat(selected).isNotNull();
        assertThat(selected.getStatus()).isEqualTo("ENABLED");
        assertThat(allPermissions)
                .extracting(BizSystemApiPermissionPO::getApiCode)
                .containsExactly(ApiCode.APP_PREVIEW_CREATE.getCode(), ApiCode.USER_FILES_LIST.getCode());
    }

    private static BizSystemPO newBizSystem(String businessSystemId, String clientId) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 26, 10, 0);
        BizSystemPO bizSystem = new BizSystemPO();
        bizSystem.setBusinessSystemId(businessSystemId);
        bizSystem.setBusinessSystemName("Contract System");
        bizSystem.setClientId(clientId);
        bizSystem.setClientSecretDigest(repeat("a", 64));
        bizSystem.setClientSecretSalt("salt-" + businessSystemId);
        bizSystem.setClientSecretAlg("HMAC-SHA256");
        bizSystem.setStatus("ENABLED");
        bizSystem.setTokenVersion(1);
        bizSystem.setPermissionVersion(1);
        bizSystem.setJwtTtlSeconds(1800);
        bizSystem.setDescription("test business system");
        bizSystem.setCreatedAt(now);
        bizSystem.setUpdatedAt(now);
        return bizSystem;
    }

    private static BizSystemApiPermissionPO newPermission(String businessSystemId, String apiCode) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 26, 10, 0);
        BizSystemApiPermissionPO permission = new BizSystemApiPermissionPO();
        permission.setBusinessSystemId(businessSystemId);
        permission.setApiCode(apiCode);
        permission.setStatus("ENABLED");
        permission.setCreatedAt(now);
        permission.setUpdatedAt(now);
        return permission;
    }

    private static String repeat(String value, int times) {
        StringBuilder builder = new StringBuilder(value.length() * times);
        for (int index = 0; index < times; index++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
