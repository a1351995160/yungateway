package com.wps.yundoc.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    void rejectsWrongClientSecret() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-token-wrong-secret");

        ResponseEntity<String> response = token(created.getBusinessSystem().getClientId(), "wrong-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
        String body = "{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}";
        return restTemplate.postForEntity(url("/api/v1/auth/token"), jsonEntity(body), String.class);
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
