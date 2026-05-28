package com.wps.yundoc.businesssystem.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    void listsBusinessSystemsWithoutSecrets() throws IOException {
        createBusinessSystem("biz-admin-list-a");
        createBusinessSystem("biz-admin-list-b");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/business-systems?page=1&pageSize=20"),
                HttpMethod.GET,
                authorized(null),
                String.class);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("items")).isNotEmpty();
        assertThat(response.getBody()).contains("biz-admin-list-a");
        assertThat(response.getBody()).doesNotContain("clientSecret");
        assertThat(response.getBody()).doesNotContain("clientSecretDigest");
        assertThat(response.getBody()).doesNotContain("clientSecretSalt");
        assertThat(body.path("pagination").path("pageSize").asInt()).isEqualTo(20);
    }

    @Test
    void filtersBusinessSystemListByKeyword() throws IOException {
        createBusinessSystem("biz-admin-keyword-hit");
        createBusinessSystem("biz-admin-keyword-miss");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/business-systems?keyword=keyword-hit&page=1&pageSize=20"),
                HttpMethod.GET,
                authorized(null),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("biz-admin-keyword-hit");
        assertThat(response.getBody()).doesNotContain("biz-admin-keyword-miss");
    }

    @Test
    void updatesBusinessSystemProfileWithoutChangingVersions() throws IOException {
        createBusinessSystem("biz-admin-update");
        BizSystemPO before = bizSystemMapper.selectByBusinessSystemId("biz-admin-update");

        MvcResult result;
        try {
            result = mockMvc.perform(patch("/api/v1/admin/business-systems/biz-admin-update")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"businessSystemName\":\"Updated System\",\"status\":\"DISABLED\","
                                    + "\"jwtTtlSeconds\":3600,\"description\":\"updated\"}"))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception ex) {
            throw new AssertionError("business system update request should succeed", ex);
        }

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        BizSystemPO after = bizSystemMapper.selectByBusinessSystemId("biz-admin-update");
        assertThat(body.path("data").path("businessSystemName").asText()).isEqualTo("Updated System");
        assertThat(body.path("data").path("status").asText()).isEqualTo("DISABLED");
        assertThat(body.path("data").path("jwtTtlSeconds").asInt()).isEqualTo(3600);
        assertThat(body.path("data").path("description").asText()).isEqualTo("updated");
        assertThat(after.getTokenVersion()).isEqualTo(before.getTokenVersion());
        assertThat(after.getPermissionVersion()).isEqualTo(before.getPermissionVersion());
    }

    @Test
    void rejectsInvalidBusinessSystemUpdateStatus() throws Exception {
        createBusinessSystem("biz-admin-invalid-update");

        mockMvc.perform(patch("/api/v1/admin/business-systems/biz-admin-invalid-update")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessSystemName\":\"Updated System\",\"status\":\"DELETED\","
                                + "\"jwtTtlSeconds\":3600}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBusinessSystemUpdateWithoutJwtTtl() throws Exception {
        createBusinessSystem("biz-admin-update-missing-ttl");

        mockMvc.perform(patch("/api/v1/admin/business-systems/biz-admin-update-missing-ttl")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessSystemName\":\"Updated System\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsCreateWithNullJwtTtl() throws Exception {
        mockMvc.perform(post("/api/v1/admin/business-systems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessSystemId\":\"biz-admin-null-ttl\","
                                + "\"businessSystemName\":\"Contract System\",\"jwtTtlSeconds\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidBusinessSystemListPage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/business-systems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt())
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest());
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
