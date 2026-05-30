package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.adminauth.application.AdminPrincipals;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.api.Pagination;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/business-systems")
public class BusinessSystemAdminController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    private final BusinessSystemAdminService businessSystemAdminService;

    public BusinessSystemAdminController(BusinessSystemAdminService businessSystemAdminService) {
        this.businessSystemAdminService = businessSystemAdminService;
    }

    @PostMapping
    public ApiResponse<BusinessSystemCreateResponse> create(
            @Valid @RequestBody BusinessSystemCreateRequest request,
            HttpServletRequest servletRequest) {
        requireBusinessSystemManager(servletRequest);
        BusinessSystemCreateResponse response = businessSystemAdminService.create(request);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @GetMapping
    public ApiResponse<BusinessSystemListResponse> list(
            @Valid BusinessSystemListRequest request,
            HttpServletRequest servletRequest) {
        BusinessSystemListResponse response = businessSystemAdminService.list(
                request,
                principal(servletRequest).getRole().canViewPermissions());
        Pagination pagination = Pagination.cursor(null, request.getPageSize(), response.isHasMore());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN, pagination);
    }

    @GetMapping("/{businessSystemId}")
    public ApiResponse<BusinessSystemResponse> get(
            @PathVariable String businessSystemId,
            HttpServletRequest servletRequest) {
        BusinessSystemResponse response = businessSystemAdminService.get(
                businessSystemId,
                principal(servletRequest).getRole().canViewPermissions());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @PatchMapping("/{businessSystemId}")
    public ApiResponse<BusinessSystemResponse> update(
            @PathVariable String businessSystemId,
            @Valid @RequestBody BusinessSystemUpdateRequest request,
            HttpServletRequest servletRequest) {
        requireBusinessSystemManager(servletRequest);
        BusinessSystemResponse response = businessSystemAdminService.update(businessSystemId, request);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @PutMapping("/{businessSystemId}/api-permissions")
    public ApiResponse<BusinessSystemResponse> savePermissions(
            @PathVariable String businessSystemId,
            @Valid @RequestBody BusinessSystemApiPermissionUpdateRequest request,
            HttpServletRequest servletRequest) {
        requirePermissionManager(servletRequest);
        BusinessSystemResponse response = businessSystemAdminService.savePermissions(businessSystemId, request);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @GetMapping("/{businessSystemId}/api-permissions")
    public ApiResponse<BusinessSystemResponse> getPermissions(
            @PathVariable String businessSystemId,
            HttpServletRequest servletRequest) {
        requirePermissionViewer(servletRequest);
        BusinessSystemResponse response = businessSystemAdminService.getPermissions(businessSystemId);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @PostMapping("/{businessSystemId}/client-secret:reset")
    public ApiResponse<BusinessSystemSecretResponse> resetClientSecret(
            @PathVariable String businessSystemId,
            HttpServletRequest servletRequest) {
        requireBusinessSystemManager(servletRequest);
        BusinessSystemSecretResponse response = businessSystemAdminService.resetClientSecret(businessSystemId);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    private void requireBusinessSystemManager(HttpServletRequest request) {
        if (!principal(request).getRole().canManageBusinessSystems()) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }

    private void requirePermissionViewer(HttpServletRequest request) {
        if (!principal(request).getRole().canViewPermissions()) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }

    private void requirePermissionManager(HttpServletRequest request) {
        if (!principal(request).getRole().canManagePermissions()) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }

    private AdminPrincipal principal(HttpServletRequest request) {
        return AdminPrincipals.require(request);
    }
}
