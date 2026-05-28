package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessSystemAdminControllerArchitectureTest {

    @Test
    void controllerDoesNotOwnAdminAuthBoundary() {
        boolean dependsOnAdminAuthService = Arrays.stream(BusinessSystemAdminController.class.getDeclaredConstructors())
                .map(Constructor::getParameterTypes)
                .flatMap(Arrays::stream)
                .anyMatch(AdminAuthService.class::equals);

        assertThat(dependsOnAdminAuthService).isFalse();
    }
}
