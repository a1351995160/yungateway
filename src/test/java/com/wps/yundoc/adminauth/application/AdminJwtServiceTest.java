package com.wps.yundoc.adminauth.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.adminauth.infrastructure.AdminAuthProperties;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminJwtServiceTest {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String JWT_SECRET = "test-admin-jwt-secret-with-enough-length";

    @Test
    void rejectsUnknownRoleClaim() {
        AdminJwtService service = new AdminJwtService(properties(), new ObjectMapper());
        String token = tokenWithRole("UNKNOWN_ROLE");

        assertThatThrownBy(() -> service.validate("Bearer " + token))
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.TOKEN_INVALID);
    }

    private AdminAuthProperties properties() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setJwtSecret(JWT_SECRET);
        properties.setJwtTtlSeconds(1800);
        return properties;
    }

    private String tokenWithRole(String role) {
        long expiresAt = Instant.now().plusSeconds(300).getEpochSecond();
        String payload = "{\"sub\":\"admin\",\"role\":\"" + role + "\",\"typ\":\"admin-jwt\",\"exp\":" + expiresAt + "}";
        String signingInput = base64Url(HEADER) + "." + base64Url(payload);
        return signingInput + "." + signature(signingInput);
    }

    private String signature(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new AssertionError("signature should be generated", ex);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
