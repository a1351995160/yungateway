package com.wps.yundoc.capability.userfile.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserFileControllerTest {

    private static final String USER_ASSERTION_SECRET = "test-user-assertion-secret-with-enough-length";

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
        AuthFixture auth = userFileAuth("biz-user-files-reauth");

        ResponseEntity<String> response = getFiles(auth, "?userId=user-001", "user-001");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.path("code").asText()).isEqualTo("REAUTH_REQUIRED");
        assertThat(error.path("details").path("authorizeUrl").asText()).contains("state=");
    }

    @ParameterizedTest
    @MethodSource("invalidUserFileQueries")
    void rejectsInvalidUserFileQuery(
            String businessSystemId,
            String query,
            String expectedCode) throws IOException {
        AuthFixture auth = userFileAuth(businessSystemId);

        ResponseEntity<String> response = getFiles(auth.accessToken, query);

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.path("code").asText()).isEqualTo(expectedCode);
    }

    @Test
    void rejectsMissingUserAssertionHeaders() throws IOException {
        AuthFixture auth = userFileAuth("biz-user-files-missing-assertion");

        ResponseEntity<String> response = getFiles(auth.accessToken, "?userId=user-001");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.path("code").asText()).isEqualTo("USER_ASSERTION_INVALID");
    }

    @Test
    void rejectsTamperedUserAssertion() throws IOException {
        AuthFixture auth = userFileAuth("biz-user-files-tampered-assertion");

        ResponseEntity<String> response = getFiles(auth, "?userId=user-002", "user-001");

        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.path("code").asText()).isEqualTo("USER_ASSERTION_INVALID");
    }

    @Test
    void rejectsReplayedUserAssertionNonce() throws IOException {
        AuthFixture auth = userFileAuth("biz-user-files-replayed-assertion");
        String nonce = "replay-nonce";

        ResponseEntity<String> first = getFiles(auth, "?userId=user-004", "user-004", nonce);
        ResponseEntity<String> second = getFiles(auth, "?userId=user-004", "user-004", nonce);

        JsonNode error = objectMapper.readTree(second.getBody()).path("error");
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(objectMapper.readTree(first.getBody()).path("error").path("code").asText())
                .isEqualTo("REAUTH_REQUIRED");
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.path("code").asText()).isEqualTo("USER_ASSERTION_INVALID");
    }

    @Test
    void callbackStoresTokenAndFileListSucceeds() throws IOException {
        AuthFixture auth = userFileAuth("biz-user-files-success");
        ResponseEntity<String> first = getFiles(auth, "?userId=user-003", "user-003");
        String state = stateFrom(first);

        ResponseEntity<String> callback = restTemplate.getForEntity(
                url("/api/v1/wps/oauth/callback?code=ok-code&state=" + state),
                String.class);
        ResponseEntity<String> second = getFiles(auth, "?userId=user-003&parentFileId=root&limit=20", "user-003");

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

    private ResponseEntity<String> getFiles(AuthFixture auth, String query, String userId) {
        return getFiles(auth, query, userId, "nonce-" + System.nanoTime());
    }

    private ResponseEntity<String> getFiles(AuthFixture auth, String query, String userId, String nonce) {
        return restTemplate.exchange(
                url("/api/v1/user/files" + query),
                HttpMethod.GET,
                signed(auth, query, userId, nonce),
                String.class);
    }

    private static Stream<Arguments> invalidUserFileQueries() {
        return Stream.of(
                Arguments.of("biz-user-files-missing-user", "", "USER_ID_REQUIRED"),
                Arguments.of(
                        "biz-user-files-bad-user",
                        "?userId=user-001&userId=user-002",
                        "VALIDATION_FAILED"),
                Arguments.of("biz-user-files-invalid-shape", "?userId=bad user", "VALIDATION_FAILED"),
                Arguments.of("biz-user-files-blank-user", "?userId=%20", "VALIDATION_FAILED"));
    }

    private String stateFrom(ResponseEntity<String> response) throws IOException {
        JsonNode error = objectMapper.readTree(response.getBody()).path("error");
        URI uri = URI.create(error.path("details").path("authorizeUrl").asText());
        return uri.getQuery().replaceFirst("^.*state=([^&]+).*$", "$1");
    }

    private AuthFixture userFileAuth(String businessSystemId) {
        BusinessSystemCreateResponse created = createBusinessSystem(businessSystemId);
        adminService.savePermissions(businessSystemId, permissions());
        return new AuthFixture(
                businessSystemId,
                created.getBusinessSystem().getClientId(),
                accessToken(created));
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

    private HttpEntity<String> signed(AuthFixture auth, String query, String userId, String nonce) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.accessToken);
        headers.add("X-Yundoc-User-Id", userId);
        headers.add("X-Yundoc-User-Timestamp", timestamp);
        headers.add("X-Yundoc-User-Nonce", nonce);
        headers.add("X-Yundoc-User-Key-Id", "v1");
        headers.add("X-Yundoc-User-Signature", signature(auth, query, userId, timestamp, nonce));
        return new HttpEntity<>(headers);
    }

    private String signature(AuthFixture auth, String query, String userId, String timestamp, String nonce) {
        String canonicalText = "GET\n"
                + "/api/v1/user/files\n"
                + queryString(query) + "\n"
                + auth.businessSystemId + "\n"
                + auth.clientId + "\n"
                + userId + "\n"
                + timestamp + "\n"
                + nonce;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(USER_ASSERTION_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(canonicalText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new AssertionError("signature should be generated", ex);
        }
    }

    private String queryString(String query) {
        if (query.startsWith("?")) {
            return query.substring(1);
        }
        return query;
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

    private static class AuthFixture {
        private final String businessSystemId;
        private final String clientId;
        private final String accessToken;

        private AuthFixture(String businessSystemId, String clientId, String accessToken) {
            this.businessSystemId = businessSystemId;
            this.clientId = clientId;
            this.accessToken = accessToken;
        }
    }
}
