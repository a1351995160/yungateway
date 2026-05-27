package com.wps.yundoc.capability.userfile.api;

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
import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserFileControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessSystemAdminService adminService;

    @Test
    void returnsReauthWhenUserTokenMissing() throws IOException {
        String token = userFileToken("biz-user-files-reauth");

        ResponseEntity<String> response = getFiles(token, "?userId=user-001");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.path("code").asText()).isEqualTo("REAUTH_REQUIRED");
        assertThat(error.path("details").path("authorizeUrl").asText()).contains("state=");
    }

    @Test
    void rejectsMissingUserId() throws IOException {
        String token = userFileToken("biz-user-files-missing-user");

        ResponseEntity<String> response = getFiles(token, "");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.path("code").asText()).isEqualTo("USER_ID_REQUIRED");
    }

    @Test
    void rejectsConflictingDuplicateUserIdQuery() throws IOException {
        String token = userFileToken("biz-user-files-bad-user");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/user/files?userId=user-001&userId=user-002"),
                HttpMethod.GET,
                authorized(token),
                String.class);

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.path("code").asText()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsInvalidQueryShape() throws IOException {
        String token = userFileToken("biz-user-files-invalid-shape");

        ResponseEntity<String> response = getFiles(token, "?userId=bad user");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.path("code").asText()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsBlankUserIdQuery() throws IOException {
        String token = userFileToken("biz-user-files-blank-user");

        ResponseEntity<String> response = getFiles(token, "?userId=%20");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.path("code").asText()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void callbackStoresTokenAndFileListSucceeds() throws IOException {
        String token = userFileToken("biz-user-files-success");
        ResponseEntity<String> first = getFiles(token, "?userId=user-003");
        String state = stateFrom(first);

        ResponseEntity<String> callback = restTemplate.getForEntity(
                url("/api/v1/wps/oauth/callback?code=ok-code&state=" + state),
                String.class);
        ResponseEntity<String> second = getFiles(token, "?userId=user-003&parentFileId=root&limit=20");

        JsonNode data = objectMapper.readTree(second.getBody()).path("data");
        assertThat(callback.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.path("items").get(0).path("fileId").asText()).isEqualTo("wps-file-001");
        assertThat(data.path("nextCursor").asText()).isEqualTo("next-cursor");
    }

    private ResponseEntity<String> getFiles(String token, String query) {
        return restTemplate.exchange(
                url("/api/v1/user/files" + query),
                HttpMethod.GET,
                authorized(token),
                String.class);
    }

    private String stateFrom(ResponseEntity<String> response) throws IOException {
        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        URI uri = URI.create(error.path("details").path("authorizeUrl").asText());
        return uri.getQuery().replaceFirst("^.*state=([^&]+).*$", "$1");
    }

    private String userFileToken(String businessSystemId) {
        BusinessSystemCreateResponse created = createBusinessSystem(businessSystemId);
        adminService.savePermissions(businessSystemId, permissions());
        return accessToken(created);
    }

    private String accessToken(BusinessSystemCreateResponse created) {
        String body = "{\"clientId\":\"" + created.getBusinessSystem().getClientId()
                + "\",\"clientSecret\":\"" + created.getClientSecret() + "\"}";
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/auth/token"),
                json(body),
                String.class);
        return readAccessToken(response);
    }

    private String readAccessToken(ResponseEntity<String> response) {
        try {
            return objectMapper.readTree(response.getBody()).path("data").path("accessToken").asText();
        } catch (IOException ex) {
            throw new AssertionError("token response must be json", ex);
        }
    }

    private BusinessSystemCreateResponse createBusinessSystem(String businessSystemId) {
        BusinessSystemCreateRequest request = new BusinessSystemCreateRequest();
        request.setBusinessSystemId(businessSystemId);
        request.setBusinessSystemName("User File System");
        request.setJwtTtlSeconds(1800);
        return adminService.create(request);
    }

    private BusinessSystemApiPermissionUpdateRequest permissions() {
        BusinessSystemApiPermissionUpdateRequest request = new BusinessSystemApiPermissionUpdateRequest();
        request.setApiPermissions(Collections.singletonList("user-files:list"));
        return request;
    }

    private HttpEntity<String> authorized(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> json(String body) {
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
