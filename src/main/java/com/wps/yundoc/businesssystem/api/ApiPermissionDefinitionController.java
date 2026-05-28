package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.businesssystem.domain.ApiPermissionDefinition;
import com.wps.yundoc.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/api-permission-definitions")
public class ApiPermissionDefinitionController {

    private static final String REQUEST_ID_UNKNOWN = "unknown";

    @GetMapping
    public ApiResponse<List<ApiPermissionDefinitionResponse>> list() {
        List<ApiPermissionDefinitionResponse> response = ApiPermissionDefinition.all().stream()
                .map(ApiPermissionDefinitionResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.success(response, REQUEST_ID_UNKNOWN);
    }
}
