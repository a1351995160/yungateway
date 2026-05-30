package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.adminauth.application.AdminPrincipals;
import com.wps.yundoc.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/admin/me")
public class AdminMeController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    @GetMapping
    public ApiResponse<AdminUserResponse> me(HttpServletRequest request) {
        AdminPrincipal principal = principal(request);
        AdminUserResponse response = AdminUserResponse.fromPrincipal(principal);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    private AdminPrincipal principal(HttpServletRequest request) {
        return AdminPrincipals.require(request);
    }
}
