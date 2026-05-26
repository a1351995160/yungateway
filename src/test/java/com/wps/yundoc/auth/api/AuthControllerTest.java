package com.wps.yundoc.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessSystemAdminService adminService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void issuesBusinessJwtWithoutUserOrAuthModeClaims() throws IOException {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-token-ok");
        adminService.savePermissions("biz-token-ok", permissions("user-files:list"));

        ResponseEntity<String> response = token(created.getBusinessSystem().getClientId(), created.getClientSecret());

        JsonNode body = objectMapper.readTree(response.getBody());
        String accessToken = body.path("data").path("accessToken").asText();
        JsonNode payload = jwtPayload(accessToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(body.path("data").path("apiPermissions").get(0).asText()).isEqualTo("user-files:list");
        assertThat(payload.path("businessSystemId").asText()).isEqualTo("biz-token-ok");
        assertThat(payload.has("userId")).isFalse();
        assertThat(payload.has("authMode")).isFalse();
    }

    @Test
    void rejectsWrongClientSecret() throws Exception {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-token-wrong-secret");

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenJson(created.getBusinessSystem().getClientId(), "wrong-secret")))
                .andExpect(status().isUnauthorized());
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

    private ResponseEntity<String> token(String clientId, String clientSecret) {
        return restTemplate.postForEntity(url("/api/v1/auth/token"), jsonEntity(tokenJson(clientId, clientSecret)), String.class);
    }

    private String tokenJson(String clientId, String clientSecret) {
        return "{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}";
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private JsonNode jwtPayload(String accessToken) throws IOException {
        String[] parts = accessToken.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readTree(new String(payload, StandardCharsets.UTF_8));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
