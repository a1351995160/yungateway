package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.adminuser.application.AdminUserService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.api.Pagination;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_UNKNOWN = "unknown";

    private final AdminAuthService adminAuthService;
    private final AdminUserService adminUserService;

    public AdminUserController(AdminAuthService adminAuthService, AdminUserService adminUserService) {
        this.adminAuthService = adminAuthService;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<AdminUserListResponse> list(
            @RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader,
            @Valid AdminUserListRequest request) {
        adminAuthService.requireSuperAdmin(authorizationHeader);
        AdminUserListResponse response = adminUserService.list(request);
        Pagination pagination = Pagination.cursor(null, request.getPageSize(), response.isHasMore());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN, pagination);
    }

    @PostMapping
    public ApiResponse<AdminUserResponse> create(
            @RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader,
            @Valid @RequestBody AdminUserCreateRequest request) {
        adminAuthService.requireSuperAdmin(authorizationHeader);
        return ApiResponse.success(adminUserService.create(request), REQUEST_ID_UNKNOWN);
    }

    @PatchMapping("/{username}")
    public ApiResponse<AdminUserResponse> update(
            @RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader,
            @PathVariable String username,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        adminAuthService.requireSuperAdmin(authorizationHeader);
        return ApiResponse.success(adminUserService.update(username, request), REQUEST_ID_UNKNOWN);
    }
}
