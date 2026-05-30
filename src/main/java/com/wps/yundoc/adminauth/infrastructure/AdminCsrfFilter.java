package com.wps.yundoc.adminauth.infrastructure;

import com.wps.yundoc.adminauth.application.AdminAuthCookieService;
import com.wps.yundoc.common.config.AdminConsoleSecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * CSRF guard for cookie-based admin console requests.
 *
 * @author wps
 */
@Component
public class AdminCsrfFilter extends OncePerRequestFilter {

    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AdminAuthCookieService cookieService;
    private final AdminConsoleSecurityProperties properties;

    public AdminCsrfFilter(
            AdminAuthCookieService cookieService,
            AdminConsoleSecurityProperties properties) {
        this.cookieService = cookieService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (requiresCsrfCheck(request) && !isValidCsrfRequest(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrfCheck(HttpServletRequest request) {
        return isAdminApi(request)
                && isUnsafeMethod(request)
                && !properties.getAdminLoginPath().equals(applicationPath(request))
                && cookieService.hasSessionCookie(request)
                && !hasText(request.getHeader(AUTHORIZATION_HEADER));
    }

    private boolean isValidCsrfRequest(HttpServletRequest request) {
        return hasAllowedOrigin(request) && hasMatchingCsrfToken(request);
    }

    private boolean hasAllowedOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (hasText(origin)) {
            return properties.getAllowedOrigins().contains(origin);
        }
        String referer = request.getHeader(HttpHeaders.REFERER);
        return !hasText(referer) || properties.getAllowedOrigins().contains(originOf(referer));
    }

    private boolean hasMatchingCsrfToken(HttpServletRequest request) {
        String headerToken = request.getHeader(CSRF_HEADER);
        return hasText(headerToken)
                && cookieService.csrfToken(request)
                .map(headerToken::equals)
                .orElse(false);
    }

    private boolean isAdminApi(HttpServletRequest request) {
        return applicationPath(request).startsWith(properties.getAdminApiPrefix());
    }

    private boolean isUnsafeMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method)
                || "PUT".equals(method)
                || "PATCH".equals(method)
                || "DELETE".equals(method);
    }

    private String applicationPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (hasText(servletPath)) {
            return servletPath;
        }
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!hasText(contextPath)) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private String originOf(String value) {
        try {
            URI uri = new URI(value);
            StringBuilder origin = new StringBuilder()
                    .append(uri.getScheme())
                    .append("://")
                    .append(uri.getHost());
            if (uri.getPort() >= 0) {
                origin.append(':').append(uri.getPort());
            }
            return origin.toString();
        } catch (URISyntaxException ex) {
            return "";
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
