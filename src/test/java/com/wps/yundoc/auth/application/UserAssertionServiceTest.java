package com.wps.yundoc.auth.application;

import com.wps.yundoc.auth.infrastructure.LocalUserAssertionNonceCache;
import com.wps.yundoc.auth.infrastructure.UserAssertionProperties;
import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class UserAssertionServiceTest {

    private static final String SECRET = "test-user-assertion-secret-with-enough-length";

    @Test
    void acceptsValidAssertionBehindContextPathUsingApplicationPath() {
        UserAssertionService service = service(properties());
        String timestamp = currentTimestamp();
        MockHttpServletRequest request = request("/gateway/api/v1/user/files", "userId=user-001");
        request.setContextPath("/gateway");
        request.setServletPath("/api/v1/user/files");
        addAssertionHeaders(request, "user-001", timestamp, "nonce-001", "/api/v1/user/files");

        assertThatCode(() -> service.verify(request, context(), "user-001")).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidSignatureWithoutConsumingNonce() {
        UserAssertionService service = service(properties());
        String timestamp = currentTimestamp();
        MockHttpServletRequest invalid = request("/api/v1/user/files", "userId=user-001");
        addAssertionHeaders(
                invalid,
                "user-001",
                timestamp,
                "nonce-001",
                "/api/v1/user/files",
                "invalid-signature");

        assertInvalidAssertion(() -> service.verify(invalid, context(), "user-001"));

        MockHttpServletRequest valid = request("/api/v1/user/files", "userId=user-001");
        addAssertionHeaders(valid, "user-001", timestamp, "nonce-001", "/api/v1/user/files");
        assertThatCode(() -> service.verify(valid, context(), "user-001")).doesNotThrowAnyException();
    }

    @Test
    void rejectsExpiredFutureMalformedAndOverflowTimestamps() {
        UserAssertionService service = service(properties());

        assertInvalidTimestamp(service, String.valueOf(Instant.now().minusSeconds(301).getEpochSecond()));
        assertInvalidTimestamp(service, String.valueOf(Instant.now().plusSeconds(301).getEpochSecond()));
        assertInvalidTimestamp(service, "not-a-timestamp");
        assertInvalidTimestamp(service, String.valueOf(Long.MIN_VALUE));
    }

    private void assertInvalidTimestamp(UserAssertionService service, String timestamp) {
        MockHttpServletRequest request = request("/api/v1/user/files", "userId=user-001");
        addAssertionHeaders(request, "user-001", timestamp, "nonce-" + timestamp, "/api/v1/user/files");

        assertInvalidAssertion(() -> service.verify(request, context(), "user-001"));
    }

    private void assertInvalidAssertion(ThrowingCallable callable) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(YundocException.class)
                .hasFieldOrPropertyWithValue("errorCode", YundocErrorCode.USER_ASSERTION_INVALID);
    }

    private UserAssertionService service(UserAssertionProperties properties) {
        return new UserAssertionService(properties, new LocalUserAssertionNonceCache(properties));
    }

    private UserAssertionProperties properties() {
        UserAssertionProperties properties = new UserAssertionProperties();
        properties.setSecret(SECRET);
        properties.setKeyId("v1");
        properties.setTimestampTolerance(Duration.ofMinutes(5));
        properties.setMaxNonceCount(100);
        return properties;
    }

    private MockHttpServletRequest request(String path, String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setQueryString(queryString);
        return request;
    }

    private void addAssertionHeaders(
            MockHttpServletRequest request,
            String userId,
            String timestamp,
            String nonce,
            String path) {
        request.addHeader(UserAssertionService.USER_ID_HEADER, userId);
        request.addHeader(UserAssertionService.TIMESTAMP_HEADER, timestamp);
        request.addHeader(UserAssertionService.NONCE_HEADER, nonce);
        request.addHeader(UserAssertionService.KEY_ID_HEADER, "v1");
        request.addHeader(
                UserAssertionService.SIGNATURE_HEADER,
                signature(path, request.getQueryString(), userId, timestamp, nonce));
    }

    private void addAssertionHeaders(
            MockHttpServletRequest request,
            String userId,
            String timestamp,
            String nonce,
            String path,
            String signature) {
        request.addHeader(UserAssertionService.USER_ID_HEADER, userId);
        request.addHeader(UserAssertionService.TIMESTAMP_HEADER, timestamp);
        request.addHeader(UserAssertionService.NONCE_HEADER, nonce);
        request.addHeader(UserAssertionService.KEY_ID_HEADER, "v1");
        request.addHeader(UserAssertionService.SIGNATURE_HEADER, signature);
    }

    private String signature(String path, String queryString, String userId, String timestamp, String nonce) {
        String canonicalText = "GET\n"
                + path + "\n"
                + queryString + "\n"
                + "biz-001\n"
                + "client-001\n"
                + userId + "\n"
                + timestamp + "\n"
                + nonce;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(canonicalText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new AssertionError("signature should be generated", ex);
        }
    }

    private RequestContext context() {
        return RequestContext.builder("request-001")
                .businessSystemId("biz-001")
                .clientId("client-001")
                .build();
    }

    private String currentTimestamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }

    private interface ThrowingCallable {
        void call();
    }
}
