package com.wps.yundoc.adminuser.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.adminuser.infrastructure.AdminUserMapper;
import com.wps.yundoc.adminuser.infrastructure.AdminUserPO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void superAdminCanReadCurrentPrincipal() throws IOException {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/me"),
                HttpMethod.GET,
                authorized(null, superAdminJwt()),
                String.class);

        JsonNode body = objectMapper.readTree(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("username").asText()).isEqualTo("admin");
        assertThat(body.path("data").path("role").asText()).isEqualTo("SUPER_ADMIN");
        assertThat(body.path("data").path("superAdmin").asBoolean()).isTrue();
    }

    @Test
    void superAdminCreatesDatabaseBackedAdminUserWithoutReturningPasswordMaterial() throws IOException {
        String username = "system-admin-create";

        ResponseEntity<String> response = createUser(username, "SYSTEM_ADMIN");
        AdminUserPO stored = adminUserMapper.selectByUsername(username);
        JsonNode body = objectMapper.readTree(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stored).isNotNull();
        assertThat(stored.getLoginDigest()).isNotBlank();
        assertThat(stored.getLoginSalt()).isNotBlank();
        assertThat(body.path("data").path("username").asText()).isEqualTo(username);
        assertThat(body.path("data").path("role").asText()).isEqualTo("SYSTEM_ADMIN");
        assertThat(response.getBody()).doesNotContain("initialPassword");
        assertThat(response.getBody()).doesNotContain("loginDigest");
        assertThat(response.getBody()).doesNotContain("loginSalt");
        assertThat(response.getBody()).doesNotContain("loginAlgorithm");
    }

    @Test
    void databaseBackedAdminUserCanLoginAndReadCurrentPrincipal() throws IOException {
        String username = "support-login";
        createUser(username, "SUPPORT");

        String token = login(username, "InitialPass123!");
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/me"),
                HttpMethod.GET,
                authorized(null, token),
                String.class);
        JsonNode body = objectMapper.readTree(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("username").asText()).isEqualTo(username);
        assertThat(body.path("data").path("role").asText()).isEqualTo("SUPPORT");
        assertThat(body.path("data").path("superAdmin").asBoolean()).isFalse();
    }

    @Test
    void nonSuperAdminCannotManageAdminUsers() {
        String username = "system-admin-denied";
        createUser(username, "SYSTEM_ADMIN");
        String systemAdminToken = login(username, "InitialPass123!");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/users"),
                authorized(createUserJson("denied-user", "SUPPORT"), systemAdminToken),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectsSuperAdminRoleForDatabaseBackedUsers() {
        ResponseEntity<String> response = createUser("illegal-super-admin", "SUPER_ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listUsersDoesNotExposePasswordMaterial() {
        createUser("auditor-list", "AUDITOR");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/admin/users?page=1&pageSize=20"),
                HttpMethod.GET,
                authorized(null, superAdminJwt()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("auditor-list");
        assertThat(response.getBody()).doesNotContain("loginDigest");
        assertThat(response.getBody()).doesNotContain("loginSalt");
        assertThat(response.getBody()).doesNotContain("loginAlgorithm");
        assertThat(response.getBody()).doesNotContain("InitialPass123!");
    }

    @Test
    void disabledUserCannotLoginOrKeepUsingExistingToken() throws Exception {
        String username = "support-disabled";
        createUser(username, "SUPPORT");
        String token = login(username, "InitialPass123!");

        mockMvc.perform(patch("/api/v1/admin/users/" + username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Support Disabled\",\"role\":\"SUPPORT\","
                                + "\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, "InitialPass123!")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());

    }

    private ResponseEntity<String> createUser(String username, String role) {
        return restTemplate.postForEntity(
                url("/api/v1/admin/users"),
                authorized(createUserJson(username, role), superAdminJwt()),
                String.class);
    }

    private String createUserJson(String username, String role) {
        return "{\"username\":\"" + username + "\",\"displayName\":\"" + username
                + "\",\"role\":\"" + role + "\",\"initialPassword\":\"InitialPass123!\"}";
    }

    private String superAdminJwt() {
        return login("admin", "admin-password");
    }

    private String login(String username, String password) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/auth/login"),
                jsonEntity(loginJson(username, password)),
                String.class);
        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            return body.path("data").path("adminJwt").asText();
        } catch (IOException ex) {
            throw new AssertionError("admin login response must be json", ex);
        }
    }

    private String loginJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private HttpEntity<String> authorized(String body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
