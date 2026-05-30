package com.wps.yundoc.adminauth.api;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.adminauth.application.AdminAuthCookieService;
import com.wps.yundoc.adminauth.application.AdminJwt;
import com.wps.yundoc.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminAuthCookieService cookieService;

    public AdminAuthController(AdminAuthService adminAuthService, AdminAuthCookieService cookieService) {
        this.adminAuthService = adminAuthService;
        this.cookieService = cookieService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AdminJwt adminJwt = adminAuthService.login(request, httpRequest.getRemoteAddr());
        cookieService.writeLoginCookie(httpRequest, httpResponse, adminJwt);
        AdminLoginResponse response = new AdminLoginResponse(adminJwt);
        return ApiResponse.success(response, "unknown");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        cookieService.clearLoginCookie(httpRequest, httpResponse);
        return ApiResponse.success(null, "unknown");
    }
}
