package com.wps.yundoc.adminauth.infrastructure;

import com.wps.yundoc.adminauth.application.AdminAuthCookieService;
import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.adminauth.application.AdminPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;
    private final AdminAuthCookieService cookieService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService, AdminAuthCookieService cookieService) {
        this.adminAuthService = adminAuthService;
        this.cookieService = cookieService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AdminPrincipal principal = adminAuthService.requireAdmin(cookieService.authorization(request).orElse(null));
        request.setAttribute(AdminPrincipal.REQUEST_ATTRIBUTE, principal);
        return true;
    }
}
