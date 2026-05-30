package com.wps.yundoc.auth.application;

import com.wps.yundoc.auth.infrastructure.LocalUserAssertionNonceCache;
import com.wps.yundoc.auth.infrastructure.UserAssertionProperties;
import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class UserAssertionService {

    public static final String USER_ID_HEADER = "X-Yundoc-User-Id";
    public static final String TIMESTAMP_HEADER = "X-Yundoc-User-Timestamp";
    public static final String NONCE_HEADER = "X-Yundoc-User-Nonce";
    public static final String SIGNATURE_HEADER = "X-Yundoc-User-Signature";
    public static final String KEY_ID_HEADER = "X-Yundoc-User-Key-Id";

    private static final String JCA_HMAC_SHA256 = "HmacSHA256";

    private final UserAssertionProperties properties;
    private final LocalUserAssertionNonceCache nonceCache;

    public UserAssertionService(
            UserAssertionProperties properties,
            LocalUserAssertionNonceCache nonceCache) {
        this.properties = properties;
        this.nonceCache = nonceCache;
    }

    public void verify(HttpServletRequest request, RequestContext context, String userId) {
        String headerUserId = requiredHeader(request, USER_ID_HEADER);
        String timestamp = requiredHeader(request, TIMESTAMP_HEADER);
        String nonce = requiredHeader(request, NONCE_HEADER);
        String signature = requiredHeader(request, SIGNATURE_HEADER);
        validateKeyId(request);
        validateUserId(userId, headerUserId);
        long epochSeconds = validateTimestamp(timestamp);
        validateSignature(request, context, userId, timestamp, nonce, signature);
        validateNonce(context, nonce, epochSeconds);
    }

    private void validateKeyId(HttpServletRequest request) {
        String keyId = request.getHeader(KEY_ID_HEADER);
        if (!hasText(keyId)) {
            return;
        }
        if (properties.getKeyId().equals(keyId.trim())) {
            return;
        }
        throw invalidAssertion();
    }

    private void validateUserId(String userId, String headerUserId) {
        if (userId.equals(headerUserId)) {
            return;
        }
        throw invalidAssertion();
    }

    private long validateTimestamp(String timestamp) {
        long epochSeconds = parseEpochSeconds(timestamp);
        long now = Instant.now().getEpochSecond();
        long toleranceSeconds = properties.getTimestampTolerance().getSeconds();
        if (epochSeconds >= now - toleranceSeconds && epochSeconds <= now + toleranceSeconds) {
            return epochSeconds;
        }
        throw invalidAssertion();
    }

    private long parseEpochSeconds(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw invalidAssertion();
        }
    }

    private void validateNonce(RequestContext context, String nonce, long epochSeconds) {
        if (nonceCache.markUsed(context.getBusinessSystemId(), context.getClientId(), nonce, epochSeconds)) {
            return;
        }
        throw invalidAssertion();
    }

    private void validateSignature(
            HttpServletRequest request,
            RequestContext context,
            String userId,
            String timestamp,
            String nonce,
            String providedSignature) {
        String expectedSignature = signature(canonicalText(request, context, userId, timestamp, nonce));
        byte[] expected = expectedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] actual = providedSignature.getBytes(StandardCharsets.UTF_8);
        if (MessageDigest.isEqual(expected, actual)) {
            return;
        }
        throw invalidAssertion();
    }

    private String canonicalText(
            HttpServletRequest request,
            RequestContext context,
            String userId,
            String timestamp,
            String nonce) {
        return request.getMethod().toUpperCase()
                + "\n" + requestPath(request)
                + "\n" + queryString(request)
                + "\n" + context.getBusinessSystemId()
                + "\n" + context.getClientId()
                + "\n" + userId
                + "\n" + timestamp
                + "\n" + nonce;
    }

    private String requestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (hasText(servletPath)) {
            return servletPath + (pathInfo == null ? "" : pathInfo);
        }
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private String queryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            return "";
        }
        return queryString;
    }

    private String signature(String canonicalText) {
        try {
            Mac mac = Mac.getInstance(JCA_HMAC_SHA256);
            byte[] key = properties.getSecret().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(key, JCA_HMAC_SHA256));
            byte[] digest = mac.doFinal(canonicalText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException ex) {
            throw new YundocException(YundocErrorCode.INTERNAL_ERROR, "User assertion signing failed", ex);
        }
    }

    private String requiredHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (hasText(value)) {
            return value.trim();
        }
        throw invalidAssertion();
    }

    private boolean hasText(String value) {
        if (value == null) {
            return false;
        }
        return !value.trim().isEmpty();
    }

    private YundocException invalidAssertion() {
        return new YundocException(YundocErrorCode.USER_ASSERTION_INVALID);
    }
}
