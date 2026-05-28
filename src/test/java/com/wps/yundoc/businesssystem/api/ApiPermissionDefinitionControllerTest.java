package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.adminauth.application.AdminJwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiPermissionDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminJwtService adminJwtService;

    @Test
    void returnsApiPermissionDefinitionsForAdmin() throws Exception {
        String response = mockMvc.perform(get("/api/v1/admin/api-permission-definitions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("app-preview:create");
        assertThat(response).contains("\"identityType\":\"APP\"");
        assertThat(response).contains("user-files:list");
        assertThat(response).contains("\"identityType\":\"USER\"");
        assertThat(response).doesNotContain("oauth");
        assertThat(response).doesNotContain("token");
        assertThat(response).doesNotContain("/api/");
        assertThat(response).doesNotContain("scope");
    }

    @Test
    void rejectsAnonymousApiPermissionDefinitionRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/api-permission-definitions"))
                .andExpect(status().isUnauthorized());
    }

    private String adminJwt() {
        return adminJwtService.issue("admin").getToken();
    }
}
