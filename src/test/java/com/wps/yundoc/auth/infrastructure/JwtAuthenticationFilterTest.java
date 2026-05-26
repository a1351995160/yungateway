package com.wps.yundoc.auth.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.context.RequestContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessSystemAdminService adminService;

    @Test
    void buildsRequestContextBeforeCapabilityController() throws IOException {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-filter-ok");
        adminService.savePermissions("biz-filter-ok", permissions("app-preview:create"));
        String token = accessToken(created);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/app/previews"),
                HttpMethod.POST,
                authorized(token),
                String.class);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.path("data").path("businessSystemId").asText()).isEqualTo("biz-filter-ok");
        assertThat(body.path("data").path("apiCode").asText()).isEqualTo("app-preview:create");
    }

    @Test
    void rejectsCapabilityRequestWithoutPermission() {
        BusinessSystemCreateResponse created = createBusinessSystem("biz-filter-denied");
        adminService.savePermissions("biz-filter-denied", permissions("user-files:list"));
        String token = accessToken(created);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/app/previews"),
                HttpMethod.POST,
                authorized(token),
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

    private HttpEntity<String> authorized(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>("{}", headers);
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @TestConfiguration
    static class CapabilityTestConfiguration {

        @Bean
        CapabilityTestController capabilityTestController() {
            return new CapabilityTestController();
        }
    }

    @RestController
    static class CapabilityTestController {

        @PostMapping("/api/v1/app/previews")
        public CapabilityContextResponse preview() {
            RequestContext context = RequestContextHolder.current()
                    .orElseThrow(() -> new IllegalStateException("request context is required"));
            return new CapabilityContextResponse(context.getBusinessSystemId(), context.getApiCode());
        }
    }

    static class CapabilityContextResponse {

        private final String businessSystemId;
        private final String apiCode;

        CapabilityContextResponse(String businessSystemId, String apiCode) {
            this.businessSystemId = businessSystemId;
            this.apiCode = apiCode;
        }

        public String getBusinessSystemId() {
            return businessSystemId;
        }

        public String getApiCode() {
            return apiCode;
        }
    }
}
