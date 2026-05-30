package com.wps.yundoc.adminauth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginReturnsAdminJwt() throws IOException {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/admin/auth/login"),
                jsonEntity(loginJson("admin-password")),
                String.class);

        JsonNode body = objectMapper.readTree(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").has("adminJwt")).isFalse();
        assertThat(body.path("data").path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(body.path("data").path("expiresInSeconds").asLong()).isPositive();
        List<String> setCookies = response.getHeaders().getValuesAsList(HttpHeaders.SET_COOKIE);
        assertThat(setCookies)
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("yundoc_admin_session=")
                        .contains("HttpOnly")
                        .contains("SameSite=Lax"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("yundoc_admin_csrf=")
                        .doesNotContain("HttpOnly")
                        .contains("SameSite=Lax"));
    }

    @Test
    void logoutClearsAdminSessionAndCsrfCookies() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/logout"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("yundoc_admin_session=")
                        .contains("Max-Age=0")
                        .contains("HttpOnly"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("yundoc_admin_csrf=")
                        .contains("Max-Age=0")
                        .doesNotContain("HttpOnly"));
    }

    @Test
    void secureLoginMarksAdminCookiesSecure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin-password")))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .isNotEmpty()
                .allSatisfy(cookie -> assertThat(cookie).contains("Secure"));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rateLimitsRepeatedFailedLoginAttempts() throws Exception {
        for (int attempt = 0; attempt < 4; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("missing-admin", "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("missing-admin", "wrong-password")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void successfulLoginResetsFailedAttemptCounter() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("admin", "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin-password")))
                .andExpect(status().isOk());

        for (int attempt = 0; attempt < 4; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("admin", "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }
    }

    private String loginJson(String password) {
        return loginJson("admin", password);
    }

    private String loginJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
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
