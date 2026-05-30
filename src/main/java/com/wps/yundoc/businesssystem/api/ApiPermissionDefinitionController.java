package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.businesssystem.domain.ApiPermissionDefinition;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/api-permission-definitions")
public class ApiPermissionDefinitionController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    @GetMapping
    public ApiResponse<List<ApiPermissionDefinitionResponse>> list(HttpServletRequest request) {
        requirePermissionViewer(request);
        List<ApiPermissionDefinitionResponse> response = ApiPermissionDefinition.all().stream()
                .map(ApiPermissionDefinitionResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }

    private void requirePermissionViewer(HttpServletRequest request) {
        Object principal = request.getAttribute(AdminPrincipal.REQUEST_ATTRIBUTE);
        if (principal instanceof AdminPrincipal
                && ((AdminPrincipal) principal).getRole().canViewPermissions()) {
            return;
        }
        throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
    }
}
