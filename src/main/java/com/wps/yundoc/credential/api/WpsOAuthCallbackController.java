package com.wps.yundoc.credential.api;

import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.context.RequestContextHolder;
import com.wps.yundoc.credential.application.WpsUserAuthorizationService;
import com.wps.yundoc.credential.domain.WpsOAuthCallbackResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wps/oauth/callback")
public class WpsOAuthCallbackController {

    private final WpsUserAuthorizationService authorizationService;

    public WpsOAuthCallbackController(WpsUserAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ApiResponse<WpsOAuthCallbackResult> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        WpsOAuthCallbackResult result = authorizationService.completeAuthorization(code, state);
        return ApiResponse.success(result, requestId());
    }

    private String requestId() {
        return RequestContextHolder.currentRequestId().orElse("unknown");
    }
}
