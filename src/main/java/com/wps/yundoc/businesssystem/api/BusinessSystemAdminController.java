package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.adminauth.application.AdminAuthService;
import com.wps.yundoc.businesssystem.application.BusinessSystemAdminService;
import com.wps.yundoc.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/business-systems")
public class BusinessSystemAdminController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    private final AdminAuthService adminAuthService;
    private final BusinessSystemAdminService businessSystemAdminService;

    public BusinessSystemAdminController(
            AdminAuthService adminAuthService,
            BusinessSystemAdminService businessSystemAdminService) {
        this.adminAuthService = adminAuthService;
        this.businessSystemAdminService = businessSystemAdminService;
    }

    @PostMapping
    public ApiResponse<BusinessSystemCreateResponse> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody BusinessSystemCreateRequest request) {
        adminAuthService.requireAdmin(authorization);
        BusinessSystemCreateResponse response = businessSystemAdminService.create(request);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @GetMapping("/{businessSystemId}")
    public ApiResponse<BusinessSystemResponse> get(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String businessSystemId) {
        adminAuthService.requireAdmin(authorization);
        BusinessSystemResponse response = businessSystemAdminService.get(businessSystemId);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @PutMapping("/{businessSystemId}/api-permissions")
    public ApiResponse<BusinessSystemResponse> savePermissions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String businessSystemId,
            @Valid @RequestBody BusinessSystemApiPermissionUpdateRequest request) {
        adminAuthService.requireAdmin(authorization);
        BusinessSystemResponse response = businessSystemAdminService.savePermissions(businessSystemId, request);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @GetMapping("/{businessSystemId}/api-permissions")
    public ApiResponse<BusinessSystemResponse> getPermissions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String businessSystemId) {
        adminAuthService.requireAdmin(authorization);
        BusinessSystemResponse response = businessSystemAdminService.getPermissions(businessSystemId);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    @PostMapping("/{businessSystemId}/client-secret:reset")
    public ApiResponse<BusinessSystemSecretResponse> resetClientSecret(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String businessSystemId) {
        adminAuthService.requireAdmin(authorization);
        BusinessSystemSecretResponse response = businessSystemAdminService.resetClientSecret(businessSystemId);
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }
}
