package com.wps.yundoc.adminauth.infrastructure;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.adminauth.application.AdminPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AdminPrincipal principal = adminAuthService.requireAdmin(request.getHeader(AUTHORIZATION_HEADER));
        request.setAttribute(AdminPrincipal.REQUEST_ATTRIBUTE, principal);
        return true;
    }
}
