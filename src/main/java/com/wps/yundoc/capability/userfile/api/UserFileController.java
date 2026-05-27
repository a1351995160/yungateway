package com.wps.yundoc.capability.userfile.api;

import com.wps.yundoc.capability.userfile.application.UserFileListCommand;
import com.wps.yundoc.capability.userfile.application.UserFileListResult;
import com.wps.yundoc.capability.userfile.application.UserFileService;
import com.wps.yundoc.common.api.ApiResponse;
import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.context.RequestContextHolder;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/files")
public class UserFileController {

    private final UserFileService userFileService;

    public UserFileController(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    @GetMapping
    public ApiResponse<UserFileListResponse> listFiles(
            @RequestParam(value = "userId", required = false) List<String> queryUserIds,
            @RequestParam(value = "parentFileId", required = false) String parentFileId,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value = "cursor", required = false) String cursor) {
        UserFileListResult result = userFileService.listFiles(command(queryUserIds, parentFileId, limit, cursor));
        return ApiResponse.success(new UserFileListResponse(result), requestId());
    }

    private UserFileListCommand command(
            List<String> queryUserIds,
            String parentFileId,
            int limit,
            String cursor) {
        return new UserFileListCommand(
                queryUserId(queryUserIds),
                businessSystemId(),
                parentFileId,
                limit,
                cursor);
    }

    private String queryUserId(List<String> queryUserIds) {
        if (hasNoQueryUserId(queryUserIds)) {
            return null;
        }
        String first = queryUserIds.get(0);
        for (String userId : queryUserIds) {
            validateSameUserId(first, userId);
        }
        return first;
    }

    private void validateSameUserId(String first, String current) {
        if (first.equals(current)) {
            return;
        }
        throw new YundocException(YundocErrorCode.VALIDATION_FAILED);
    }

    private String businessSystemId() {
        return requestContext().getBusinessSystemId();
    }

    private String requestId() {
        return requestContext().getRequestId();
    }

    private RequestContext requestContext() {
        return RequestContextHolder.current()
                .orElseThrow(() -> new YundocException(YundocErrorCode.TOKEN_INVALID));
    }

    private boolean hasText(String value) {
        if (value == null) {
            return false;
        }
        return !value.trim().isEmpty();
    }

    private boolean hasNoQueryUserId(List<String> queryUserIds) {
        if (queryUserIds == null) {
            return true;
        }
        return queryUserIds.isEmpty();
    }
}
