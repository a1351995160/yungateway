package com.wps.yundoc.businesssystem.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiPermissionDefinitionTest {

    @Test
    void containsMvpAppAndUserApiCodes() {
        assertThat(ApiPermissionDefinition.allCodes())
                .contains(
                        "app-preview:create",
                        "user-files:list",
                        "user-files:rename",
                        "user-files:download",
                        "user-folders:rename",
                        "user-files:create",
                        "user-files:save-as",
                        "user-files:view",
                        "user-files:delete",
                        "user-files:update");
    }

    @Test
    void mapsApiCodeToWpsIdentityType() {
        assertThat(ApiPermissionDefinition.identityTypeOf("app-preview:create"))
                .isEqualTo(WpsIdentityType.APP);
        assertThat(ApiPermissionDefinition.identityTypeOf("user-files:list"))
                .isEqualTo(WpsIdentityType.USER);
    }

    @Test
    void rejectsUnknownApiCode() {
        assertThat(ApiPermissionDefinition.exists("unknown:api")).isFalse();
        assertThatThrownBy(() -> ApiPermissionDefinition.identityTypeOf("unknown:api"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown api code");
    }
}
