package com.wps.yundoc.capability.userfile.api;

import com.wps.yundoc.capability.userfile.application.UserFileService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.context.RequestContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/files")
public class UserFileController {

    private final UserFileService userFileService;

    public UserFileController(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    @GetMapping
    public ApiResponse<UserFileListResponse> list(@RequestParam(value = "userId", required = false) String userId) {
        UserFileListResponse response = userFileService.list(userId);
        return ApiResponse.success(response, requestId());
    }

    private String requestId() {
        return RequestContextHolder.currentRequestId().orElse("unknown");
    }
}
