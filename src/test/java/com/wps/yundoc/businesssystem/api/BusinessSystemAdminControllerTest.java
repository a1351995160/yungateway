package com.wps.yundoc.businesssystem.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemMapper;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemPO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusinessSystemAdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BizSystemMapper bizSystemMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void controllerDoesNotOwnAdminAuthBoundary() {
        boolean dependsOnAdminAuthService = Arrays.stream(BusinessSystemAdminController.class.getDeclaredConstructors())
                .map(Constructor::getParameterTypes)
                .flatMap(Arrays::stream)
                .anyMatch(AdminAuthService.class::equals);

        assertThat(dependsOnAdminAuthService).isFalse();
    }

    @Test
    void rejectsAnonymousAdminRequest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/business-systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("biz-anonymous")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsBusinessSystemWithOneTimeClientSecret() throws IOException {
        ResponseEntity<String> created = createBusinessSystem("biz-admin-create");
        JsonNode body = objectMapper.readTree(created.getBody());
        BizSystemPO stored = bizSystemMapper.selectByBusinessSystemId("biz-admin-create");

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("clientSecret").asText()).isNotBlank();
        assertThat(stored.getClientSecretDigest()).doesNotContain(body.path("data").path("clientSecret").asText());
    }

    @Test
    void rejectsDuplicateBusinessSystemId() {
        createBusinessSystem("biz-admin-duplicate");

        ResponseEntity<String> duplicate = createBusinessSystem("biz-admin-duplicate");

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void detailDoesNotReturnClientSecret() throws IOException {
        createBusinessSystem("biz-admin-detail");

        ResponseEntity<String> detail = restTemplate.exchange(
                url("/api/v1/admin/business-systems/biz-admin-detail"),
                HttpMethod.GET,
                authorized(null),
                String.class);

        JsonNode body = objectMapper.readTree(detail.getBody());
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").has("clientSecret")).isFalse();
    }

    @Test
    void rejectsUnknownApiPermission() {
        createBusinessSystem("biz-admin-invalid-permission");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/business-systems/biz-admin-invalid-permission/api-permissions"),
                HttpMethod.PUT,
                authorized("{\"apiPermissions\":[\"unknown:api\"]}"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void savesApiPermissionsAndIncrementsVersion() throws IOException {
        createBusinessSystem("biz-admin-permission");
        Integer before = bizSystemMapper.selectByBusinessSystemId("biz-admin-permission").getPermissionVersion();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/business-systems/biz-admin-permission/api-permissions"),
                HttpMethod.PUT,
                authorized("{\"apiPermissions\":[\"user-files:list\"]}"),
                String.class);

        JsonNode body = objectMapper.readTree(response.getBody());
        Integer after = bizSystemMapper.selectByBusinessSystemId("biz-admin-permission").getPermissionVersion();
        assertThat(after).isEqualTo(before + 1);
        assertThat(body.path("data").path("apiPermissions").get(0).asText()).isEqualTo("user-files:list");
    }

    private ResponseEntity<String> createBusinessSystem(String businessSystemId) {
        return restTemplate.postForEntity(
                url("/api/v1/admin/business-systems"),
                authorized(createJson(businessSystemId)),
                String.class);
    }

    private HttpEntity<String> authorized(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminJwt());
        return new HttpEntity<>(body, headers);
    }

    private String adminJwt() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/auth/login"),
                jsonEntity("{\"username\":\"admin\",\"password\":\"admin-password\"}"),
                String.class);
        return readAdminJwt(response);
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private String readAdminJwt(ResponseEntity<String> response) {
        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            return body.path("data").path("adminJwt").asText();
        } catch (IOException ex) {
            throw new AssertionError("admin login response must be json", ex);
        }
    }

    private String createJson(String businessSystemId) {
        return "{\"businessSystemId\":\"" + businessSystemId
                + "\",\"businessSystemName\":\"Contract System\",\"jwtTtlSeconds\":1800}";
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
