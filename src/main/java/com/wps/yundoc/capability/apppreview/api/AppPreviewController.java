package com.wps.yundoc.capability.apppreview.api;

import com.wps.yundoc.capability.apppreview.application.AppPreviewService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.context.RequestContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/app/previews")
public class AppPreviewController {

    private final AppPreviewService appPreviewService;

    public AppPreviewController(AppPreviewService appPreviewService) {
        this.appPreviewService = appPreviewService;
    }

    @PostMapping
    public ApiResponse<AppPreviewResponse> create(@Valid @RequestBody AppPreviewRequest request) {
        AppPreviewResponse response = appPreviewService.create(request);
        return ApiResponse.success(response, requestId());
    }

    private String requestId() {
        return RequestContextHolder.currentRequestId().orElse("unknown");
    }
}
