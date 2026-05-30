package com.wps.yundoc.mvp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MvpSmokeTest {

    private static final String USER_ASSERTION_SECRET = "test-user-assertion-secret-with-enough-length";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void completesMvpFlowWithMockWps() throws IOException {
        AdminCookies adminCookies = adminCookies();
        JsonNode created = createBusinessSystem(adminCookies, "biz-mvp-smoke");
        configurePermissions(adminCookies, "biz-mvp-smoke");
        AuthFixture auth = authFixture(created, "biz-mvp-smoke");

        JsonNode preview = postAppPreview(auth.accessToken);
        JsonNode reauth = getUserFiles(auth, HttpStatus.UNAUTHORIZED);
        String state = stateFromAuthorizeUrl(reauth);
        callback(state);
        JsonNode files = getUserFiles(auth, HttpStatus.OK);

        assertThat(preview.path("data").path("previewUrl").asText()).startsWith("https://preview.test/files/");
        assertThat(preview.path("data").path("expireAt").asText()).isNotBlank();
        assertThat(reauth.path("error").path("code").asText()).isEqualTo("REAUTH_REQUIRED");
        assertThat(reauth.path("error").path("details").path("authorizeUrl").asText()).contains("state=");
        assertThat(files.path("data").path("items")).hasSizeGreaterThan(0);
        assertThat(files.path("data").path("items").get(0).path("fileId").asText()).isNotBlank();
    }

    private AdminCookies adminCookies() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/auth/login"),
                jsonEntity("{\"username\":\"admin\",\"password\":\"admin-password\"}"),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sessionCookie = cookiePair(response, "yundoc_admin_session");
        String csrfCookie = cookiePair(response, "yundoc_admin_csrf");
        return new AdminCookies(sessionCookie + "; " + csrfCookie, cookieValue(csrfCookie));
    }

    private JsonNode createBusinessSystem(AdminCookies adminCookies, String businessSystemId) throws IOException {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/business-systems"),
                authorized(adminCookies, "{\"businessSystemId\":\"" + businessSystemId
                        + "\",\"businessSystemName\":\"MVP Smoke\",\"jwtTtlSeconds\":1800}"),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return body(response);
    }

    private void configurePermissions(AdminCookies adminCookies, String businessSystemId) {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/business-systems/" + businessSystemId + "/api-permissions"),
                HttpMethod.PUT,
                authorized(adminCookies, "{\"apiPermissions\":[\"app-preview:create\",\"user-files:list\"]}"),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String accessToken(JsonNode created) throws IOException {
        String clientId = created.path("data").path("businessSystem").path("clientId").asText();
        String clientSecret = created.path("data").path("clientSecret").asText();
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/auth/token"),
                jsonEntity("{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}"),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return body(response).path("data").path("accessToken").asText();
    }

    private AuthFixture authFixture(JsonNode created, String businessSystemId) throws IOException {
        String clientId = created.path("data").path("businessSystem").path("clientId").asText();
        return new AuthFixture(businessSystemId, clientId, accessToken(created));
    }

    private JsonNode postAppPreview(String accessToken) throws IOException {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/app/previews"),
                bearer(accessToken, previewJson("wps-file-001")),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return body(response);
    }

    private JsonNode getUserFiles(AuthFixture auth, HttpStatus expectedStatus) throws IOException {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/user/files?userId=smoke-user"),
                HttpMethod.GET,
                signed(auth),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return body(response);
    }

    private String stateFromAuthorizeUrl(JsonNode reauth) {
        String authorizeUrl = reauth.path("error").path("details").path("authorizeUrl").asText();
        return UriComponentsBuilder.fromUriString(authorizeUrl).build().getQueryParams().getFirst("state");
    }

    private void callback(String state) {
        ResponseEntity<String> response = restTemplate.getForEntity(
                URI.create(url("/api/v1/wps/oauth/callback?code=mock-code&state=" + state)),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String previewJson(String fileId) {
        return "{\"source\":{\"type\":\"WPS_FILE\",\"fileId\":\"" + fileId
                + "\"},\"options\":{\"expireSeconds\":3600}}";
    }

    private HttpEntity<String> authorized(AdminCookies adminCookies, String body) {
        HttpHeaders headers = jsonHeaders();
        headers.add(HttpHeaders.COOKIE, adminCookies.cookieHeader);
        headers.add("X-CSRF-Token", adminCookies.csrfToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<String> bearer(String accessToken, String body) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<String> signed(AuthFixture auth) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-" + System.nanoTime();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.accessToken);
        headers.add("X-Yundoc-User-Id", "smoke-user");
        headers.add("X-Yundoc-User-Timestamp", timestamp);
        headers.add("X-Yundoc-User-Nonce", nonce);
        headers.add("X-Yundoc-User-Key-Id", "v1");
        headers.add("X-Yundoc-User-Signature", signature(auth, timestamp, nonce));
        return new HttpEntity<>(null, headers);
    }

    private String signature(AuthFixture auth, String timestamp, String nonce) {
        String canonicalText = "GET\n"
                + "/api/v1/user/files\n"
                + "userId=smoke-user\n"
                + auth.businessSystemId + "\n"
                + auth.clientId + "\n"
                + "smoke-user\n"
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

    private HttpEntity<String> jsonEntity(String body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private JsonNode body(ResponseEntity<String> response) throws IOException {
        return objectMapper.readTree(response.getBody());
    }

    private String cookiePair(ResponseEntity<String> response, String name) {
        return response.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(cookie -> cookie.startsWith(name + "="))
                .map(cookie -> cookie.split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing cookie " + name));
    }

    private String cookieValue(String cookiePair) {
        return cookiePair.substring(cookiePair.indexOf('=') + 1);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static class AdminCookies {
        private final String cookieHeader;
        private final String csrfToken;

        private AdminCookies(String cookieHeader, String csrfToken) {
            this.cookieHeader = cookieHeader;
            this.csrfToken = csrfToken;
        }
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
