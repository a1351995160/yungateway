package com.wps.yundoc.auth.infrastructure;

import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

class CapabilityRouteRule {

    private static final String PATH_SEPARATOR = "/";

    private final HttpMethod method;
    private final String path;
    private final String suffix;
    private final RouteMatchType matchType;
    private final String apiCode;

    CapabilityRouteRule(
            HttpMethod method,
            String path,
            String suffix,
            RouteMatchType matchType,
            String apiCode) {
        this.method = method;
        this.path = path;
        this.suffix = suffix;
        this.matchType = matchType;
        this.apiCode = apiCode;
    }

    boolean matches(HttpServletRequest request) {
        if (!method.matches(request.getMethod())) {
            return false;
        }
        return matchesPath(applicationPath(request));
    }

    String apiCode() {
        return apiCode;
    }

    private boolean matchesPath(String requestPath) {
        String normalizedPath = normalizedPath(requestPath);
        if (matchType == RouteMatchType.EXACT) {
            return path.equals(normalizedPath);
        }
        if (matchType == RouteMatchType.PREFIX) {
            return normalizedPath.startsWith(path);
        }
        return matchesSuffixRoute(normalizedPath);
    }

    private boolean matchesSuffixRoute(String requestPath) {
        if (!requestPath.startsWith(path)) {
            return false;
        }
        return requestPath.endsWith(suffix);
    }

    private String applicationPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (hasText(servletPath)) {
            return servletPath;
        }
        return removeContextPath(request);
    }

    private String removeContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (!hasText(contextPath)) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private boolean hasText(String value) {
        if (value == null) {
            return false;
        }
        return !value.isEmpty();
    }

    private String normalizedPath(String requestPath) {
        if (requestPath == null || requestPath.length() <= 1 || !requestPath.endsWith(PATH_SEPARATOR)) {
            return requestPath;
        }
        return requestPath.substring(0, requestPath.length() - 1);
    }
}
