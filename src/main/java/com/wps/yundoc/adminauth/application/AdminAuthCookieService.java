package com.wps.yundoc.adminauth.application;

import com.wps.yundoc.common.config.AdminConsoleSecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Admin session cookie helper.
 *
 * @author wps
 */
@Component
public class AdminAuthCookieService {

    private static final String COOKIE_NAME = "yundoc_admin_session";
    private static final String CSRF_COOKIE_NAME = "yundoc_admin_csrf";
    private static final String ROOT_COOKIE_PATH = "/";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminConsoleSecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminAuthCookieService(AdminConsoleSecurityProperties properties) {
        this.properties = properties;
    }

    public void writeLoginCookie(HttpServletRequest request, HttpServletResponse response, AdminJwt adminJwt) {
        boolean secure = secureCookie(request);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie(
                        COOKIE_NAME,
                        adminJwt.getToken(),
                        adminJwt.getExpiresInSeconds(),
                        properties.getAdminCookiePath(),
                        true,
                        secure));
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie(CSRF_COOKIE_NAME, newCsrfToken(), adminJwt.getExpiresInSeconds(), ROOT_COOKIE_PATH, false, secure));
    }

    public void clearLoginCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean secure = secureCookie(request);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie(COOKIE_NAME, "", 0, properties.getAdminCookiePath(), true, secure));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(CSRF_COOKIE_NAME, "", 0, ROOT_COOKIE_PATH, false, secure));
    }

    public Optional<String> authorization(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (hasText(authorization)) {
            return Optional.of(authorization);
        }
        return cookieValue(request).map(value -> BEARER_PREFIX + value);
    }

    public boolean hasSessionCookie(HttpServletRequest request) {
        return cookieValue(request, COOKIE_NAME).isPresent();
    }

    public Optional<String> csrfToken(HttpServletRequest request) {
        return cookieValue(request, CSRF_COOKIE_NAME);
    }

    private Optional<String> cookieValue(HttpServletRequest request) {
        return cookieValue(request, COOKIE_NAME);
    }

    private Optional<String> cookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && hasText(cookie.getValue())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private String cookie(
            String name,
            String value,
            long maxAgeSeconds,
            String path,
            boolean httpOnly,
            boolean secure) {
        StringBuilder cookie = new StringBuilder()
                .append(name)
                .append('=')
                .append(value)
                .append("; Max-Age=")
                .append(maxAgeSeconds)
                .append("; Path=")
                .append(path);
        if (httpOnly) {
            cookie.append("; HttpOnly");
        }
        cookie.append("; SameSite=Lax");
        if (secure) {
            cookie.append("; Secure");
        }
        return cookie.toString();
    }

    private String newCsrfToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private boolean secureCookie(HttpServletRequest request) {
        return properties.isSecureCookies() || request.isSecure();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
