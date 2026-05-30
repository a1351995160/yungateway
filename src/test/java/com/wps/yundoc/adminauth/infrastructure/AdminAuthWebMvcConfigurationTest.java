package com.wps.yundoc.adminauth.infrastructure;

import com.wps.yundoc.adminauth.api.AdminAuthController;
import com.wps.yundoc.adminauth.api.AdminLoginRequest;
import com.wps.yundoc.adminauth.application.AdminAuthCookieService;
import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.adminauth.application.AdminJwt;
import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.businesssystem.api.BusinessSystemAdminController;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemResponse;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import com.wps.yundoc.auth.infrastructure.JwtAuthenticationFilter;
import com.wps.yundoc.common.config.AdminConsoleSecurityProperties;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                BusinessSystemAdminController.class,
                AdminAuthController.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import({
        AdminAuthCookieService.class,
        AdminAuthInterceptor.class,
        AdminAuthWebMvcConfiguration.class
})
@EnableConfigurationProperties(AdminConsoleSecurityProperties.class)
class AdminAuthWebMvcConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthService adminAuthService;

    @MockBean
    private BusinessSystemAdminService businessSystemAdminService;

    @Test
    void protectsAdminEndpointsWithAdminAuthInterceptor() throws Exception {
        doThrow(new YundocException(YundocErrorCode.AUTH_REQUIRED))
                .when(adminAuthService)
                .requireAdmin(null);

        mockMvc.perform(post("/api/v1/admin/business-systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("biz-no-admin-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));

        verify(adminAuthService).requireAdmin(null);
        verifyNoInteractions(businessSystemAdminService);
    }

    @Test
    void allowsAdminEndpointAfterAdminTokenValidation() throws Exception {
        when(adminAuthService.requireAdmin("Bearer admin-token"))
                .thenReturn(AdminPrincipal.superAdmin("admin"));
        when(businessSystemAdminService.create(any())).thenReturn(createResponse("biz-admin-token"));

        mockMvc.perform(post("/api/v1/admin/business-systems")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("biz-admin-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clientSecret").value("one-time-secret"));

        verify(adminAuthService).requireAdmin("Bearer admin-token");
    }

    @Test
    void excludesAdminLoginFromAdminAuthInterceptor() throws Exception {
        when(adminAuthService.login(any(AdminLoginRequest.class), any()))
                .thenReturn(new AdminJwt("admin-jwt", 1800));

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminJwt").doesNotExist())
                .andExpect(jsonPath("$.data.expiresInSeconds").value(1800));

        verify(adminAuthService, never()).requireAdmin(any());
        verify(adminAuthService).login(any(AdminLoginRequest.class), any());
    }

    private BusinessSystemCreateResponse createResponse(String businessSystemId) {
        BusinessSystemResponse businessSystem = new BusinessSystemResponse();
        businessSystem.setBusinessSystemId(businessSystemId);
        businessSystem.setBusinessSystemName("Contract System");
        businessSystem.setClientId("client-id");
        businessSystem.setJwtTtlSeconds(1800);
        return new BusinessSystemCreateResponse(businessSystem, "one-time-secret");
    }

    private String createJson(String businessSystemId) {
        return "{\"businessSystemId\":\"" + businessSystemId
                + "\",\"businessSystemName\":\"Contract System\",\"jwtTtlSeconds\":1800}";
    }
}
