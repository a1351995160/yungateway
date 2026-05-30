package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.adminauth.application.AdminPrincipals;
import com.wps.yundoc.adminuser.application.AdminUserService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.api.Pagination;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<AdminUserListResponse> list(
            @Valid AdminUserListRequest request,
            HttpServletRequest servletRequest) {
        requireSuperAdmin(servletRequest);
        AdminUserListResponse response = adminUserService.list(request);
        Pagination pagination = Pagination.cursor(null, request.getPageSize(), response.isHasMore());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN, pagination);
    }

    @PostMapping
    public ApiResponse<AdminUserResponse> create(
            @Valid @RequestBody AdminUserCreateRequest request,
            HttpServletRequest servletRequest) {
        requireSuperAdmin(servletRequest);
        return ApiResponse.success(adminUserService.create(request), REQUEST_ID_UNKNOWN);
    }

    @PatchMapping("/{username}")
    public ApiResponse<AdminUserResponse> update(
            @PathVariable String username,
            @Valid @RequestBody AdminUserUpdateRequest request,
            HttpServletRequest servletRequest) {
        requireSuperAdmin(servletRequest);
        return ApiResponse.success(adminUserService.update(username, request), REQUEST_ID_UNKNOWN);
    }

    private void requireSuperAdmin(HttpServletRequest request) {
        AdminPrincipal principal = AdminPrincipals.require(request);
        if (!principal.isSuperAdmin()) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }
}
