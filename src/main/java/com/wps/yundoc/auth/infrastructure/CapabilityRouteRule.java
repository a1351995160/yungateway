package com.wps.yundoc.auth.infrastructure;

import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

class CapabilityRouteRule {

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
        return matchesPath(request.getRequestURI());
    }

    String apiCode() {
        return apiCode;
    }

    private boolean matchesPath(String requestPath) {
        if (matchType == RouteMatchType.EXACT) {
            return path.equals(requestPath);
        }
        if (matchType == RouteMatchType.PREFIX) {
            return requestPath.startsWith(path);
        }
        return matchesSuffixRoute(requestPath);
    }

    private boolean matchesSuffixRoute(String requestPath) {
        if (!requestPath.startsWith(path)) {
            return false;
        }
        return requestPath.endsWith(suffix);
    }
}
