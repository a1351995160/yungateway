package com.wps.yundoc.auth.infrastructure;

import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.context.RequestContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayRequestContextFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        RequestContextHolder.set(RequestContext.builder(requestId(request)).build());
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (hasText(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private boolean hasText(String value) {
        if (value == null) {
            return false;
        }
        return !value.trim().isEmpty();
    }
}
