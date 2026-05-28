package com.wps.yundoc.adminauth.infrastructure;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminAuthInterceptorTest {

    private static final Object HANDLER = new Object();

    @Test
    void validatesAuthorizationHeader() throws Exception {
        AdminAuthService adminAuthService = mock(AdminAuthService.class);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(adminAuthService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer admin-token");

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), HANDLER);

        assertThat(allowed).isTrue();
        verify(adminAuthService).requireAdmin("Bearer admin-token");
    }

    @Test
    void propagatesAdminAuthFailure() {
        AdminAuthService adminAuthService = mock(AdminAuthService.class);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(adminAuthService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer business-token");
        doThrow(new YundocException(YundocErrorCode.TOKEN_INVALID))
                .when(adminAuthService)
                .requireAdmin("Bearer business-token");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), HANDLER))
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.TOKEN_INVALID);
    }
}
