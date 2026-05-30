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
    private static final String TOKEN_TYPE = "admin-jwt";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int JWT_PART_COUNT = 3;
    private static final String CLAIM_EXPIRY = "exp";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SUBJECT = "sub";
    private static final String CLAIM_TYPE = "typ";

    private final AdminAuthProperties properties;
    private final ObjectMapper objectMapper;

    public AdminJwtService(AdminAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public AdminJwt issue(String username) {
        return issue(username, AdminRole.SUPER_ADMIN);
    }

    public AdminJwt issue(String username, AdminRole role) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + properties.getJwtTtlSeconds();
        String payload = encodeJson(payload(username, role, issuedAt, expiresAt));
        String signingInput = base64Url(HEADER) + "." + payload;
        return new AdminJwt(signingInput + "." + signature(signingInput), properties.getJwtTtlSeconds());
    }

    public AdminPrincipal validate(String authorizationHeader) {
        String token = bearerToken(authorizationHeader);
        String[] parts = token.split("\\.");
        validateFormat(parts);
        validateSignature(parts);
        return validatePayload(parts[1]);
    }

    private Map<String, Object> payload(String username, AdminRole role, long issuedAt, long expiresAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(CLAIM_SUBJECT, username);
        payload.put(CLAIM_ROLE, role.name());
        payload.put("iat", issuedAt);
        payload.put(CLAIM_EXPIRY, expiresAt);
        payload.put(CLAIM_TYPE, TOKEN_TYPE);
        return payload;
    }

    private String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new YundocException(YundocErrorCode.AUTH_REQUIRED);
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new YundocException(YundocErrorCode.AUTH_REQUIRED);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private void validateFormat(String[] parts) {
        if (parts.length != JWT_PART_COUNT) {
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

    private AdminPrincipal validatePayload(String encodedPayload) {
        JsonNode payload = readPayload(encodedPayload);
        boolean adminToken = TOKEN_TYPE.equals(payload.path(CLAIM_TYPE).asText());
        boolean unexpired = payload.path(CLAIM_EXPIRY).asLong() > Instant.now().getEpochSecond();
        String username = payload.path(CLAIM_SUBJECT).asText();
        AdminRole role = parseRole(payload.path(CLAIM_ROLE).asText());
        if (!adminToken) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        if (username == null || username.trim().isEmpty()) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        if (!unexpired) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        return new AdminPrincipal(username, username, role, AdminStatus.ENABLED, null);
    }

    private AdminRole parseRole(String role) {
        try {
            return AdminRole.valueOf(role);
        } catch (RuntimeException ex) {
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
