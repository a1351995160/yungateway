package com.wps.yundoc.capability.apppreview.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AppPreviewControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessSystemAdminService adminService;

    @Test
    void createsAppPreviewWhenBusinessSystemHasPermission() throws IOException {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-app-preview-ok");
        adminService.savePermissions("biz-app-preview-ok", permissions("app-preview:create"));
        String token = accessToken(created);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/app/previews"),
                HttpMethod.POST,
                authorized(token, previewJson("wps-file-001")),
                String.class);

        JsonNode data = objectMapper.readTree(response.getBody()).path("data");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.path("previewUrl").asText()).contains("wps-file-001");
        assertThat(data.path("expireAt").asText()).isNotBlank();
    }

    @Test
    void rejectsInvalidPreviewRequestBody() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-app-preview-invalid");
        adminService.savePermissions("biz-app-preview-invalid", permissions("app-preview:create"));

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/app/previews"),
                HttpMethod.POST,
                authorized(accessToken(created), "{\"source\":{\"type\":\"WPS_FILE\"}}"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsAppPreviewWithoutPermission() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-app-preview-denied");
        adminService.savePermissions("biz-app-preview-denied", permissions("user-files:list"));

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/app/previews"),
                HttpMethod.POST,
                authorized(accessToken(created), previewJson("wps-file-002")),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String accessToken(BusinessSystemCreateResponse created) {
        String body = "{\"clientId\":\"" + created.getBusinessSystem().getClientId()
                + "\",\"clientSecret\":\"" + created.getClientSecret() + "\"}";
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/auth/token"),
                jsonEntity(body),
                String.class);
        return readAccessToken(response);
    }

    private String readAccessToken(ResponseEntity<String> response) {
        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            return body.path("data").path("accessToken").asText();
        } catch (IOException ex) {
            throw new AssertionError("token response must be json", ex);
        }
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

    private String previewJson(String fileId) {
        return "{\"source\":{\"type\":\"WPS_FILE\",\"fileId\":\"" + fileId
                + "\"},\"options\":{\"expireSeconds\":3600},\"userId\":\"ignored\"}";
    }

    private HttpEntity<String> authorized(String token, String body) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<String> jsonEntity(String body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
