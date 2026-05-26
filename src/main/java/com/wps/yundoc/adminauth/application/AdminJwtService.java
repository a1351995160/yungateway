package com.wps.yundoc.adminauth.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wps.yundoc.adminauth.infrastructure.AdminAuthProperties;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminJwtService {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String JCA_HMAC_SHA256 = "HmacSHA256";
    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminAuthProperties properties;
    private final ObjectMapper objectMapper;

    public AdminJwtService(AdminAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public AdminJwt issue(String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + properties.getJwtTtlSeconds();
        String payload = encodeJson(payload(username, issuedAt, expiresAt));
        String signingInput = base64Url(HEADER) + "." + payload;
        return new AdminJwt(signingInput + "." + signature(signingInput), properties.getJwtTtlSeconds());
    }

    public void validate(String authorizationHeader) {
        String token = bearerToken(authorizationHeader);
        String[] parts = token.split("\\.");
        validateFormat(parts);
        validateSignature(parts);
        validatePayload(parts[1]);
    }

    private Map<String, Object> payload(String username, long issuedAt, long expiresAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", username);
        payload.put("role", ROLE_ADMIN);
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);
        payload.put("typ", "admin-jwt");
        return payload;
    }

    private String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new YundocException(YundocErrorCode.AUTH_REQUIRED);
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new YundocException(YundocErrorCode.AUTH_REQUIRED);
        }
        return authorizationHeader.substring("Bearer ".length());
    }

    private void validateFormat(String[] parts) {
        if (parts.length != 3) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private void validateSignature(String[] parts) {
        String signingInput = parts[0] + "." + parts[1];
        byte[] actual = signature(signingInput).getBytes(StandardCharsets.UTF_8);
        byte[] expected = parts[2].getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(actual, expected)) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private void validatePayload(String encodedPayload) {
        JsonNode payload = readPayload(encodedPayload);
        boolean adminToken = ROLE_ADMIN.equals(payload.path("role").asText());
        boolean unexpired = payload.path("exp").asLong() > Instant.now().getEpochSecond();
        if (!adminToken) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        if (!unexpired) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private JsonNode readPayload(String encodedPayload) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readTree(json);
        } catch (java.io.IOException | IllegalArgumentException ex) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return base64Url(objectMapper.writeValueAsString(value));
        } catch (java.io.IOException ex) {
            throw new YundocException(YundocErrorCode.INTERNAL_ERROR, "Admin token creation failed", ex);
        }
    }

    private String signature(String signingInput) {
        try {
            Mac mac = Mac.getInstance(JCA_HMAC_SHA256);
            byte[] key = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
            return base64Url(mac.doFinal(keyedInput(mac, key, signingInput)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new YundocException(YundocErrorCode.INTERNAL_ERROR, "Admin token signing failed", ex);
        }
    }

    private byte[] keyedInput(Mac mac, byte[] key, String signingInput)
            throws java.security.InvalidKeyException {
        mac.init(new SecretKeySpec(key, JCA_HMAC_SHA256));
        return signingInput.getBytes(StandardCharsets.UTF_8);
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
