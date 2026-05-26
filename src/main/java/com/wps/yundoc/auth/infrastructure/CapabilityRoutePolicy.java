package com.wps.yundoc.auth.infrastructure;

import com.wps.yundoc.businesssystem.domain.ApiCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CapabilityRoutePolicy {

    private final List<CapabilityRouteRule> rules = Arrays.asList(
            exact(HttpMethod.POST, "/api/v1/app/previews", ApiCode.APP_PREVIEW_CREATE),
            exact(HttpMethod.GET, "/api/v1/user/files", ApiCode.USER_FILES_LIST),
            suffix(HttpMethod.PATCH, "/api/v1/user/files/", "/name", ApiCode.USER_FILES_RENAME),
            suffix(HttpMethod.POST, "/api/v1/user/files/", "/download-url", ApiCode.USER_FILES_DOWNLOAD),
            suffix(HttpMethod.PATCH, "/api/v1/user/folders/", "/name", ApiCode.USER_FOLDERS_RENAME),
            exact(HttpMethod.POST, "/api/v1/user/files", ApiCode.USER_FILES_CREATE),
            suffix(HttpMethod.POST, "/api/v1/user/files/", "/save-as", ApiCode.USER_FILES_SAVE_AS),
            suffix(HttpMethod.POST, "/api/v1/user/files/", "/view-url", ApiCode.USER_FILES_VIEW),
            prefix(HttpMethod.DELETE, "/api/v1/user/files/", ApiCode.USER_FILES_DELETE),
            suffix(HttpMethod.PUT, "/api/v1/user/files/", "/content", ApiCode.USER_FILES_UPDATE));

    public Optional<String> resolve(HttpServletRequest request) {
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .map(CapabilityRouteRule::apiCode)
                .findFirst();
    }

    private CapabilityRouteRule exact(HttpMethod method, String path, ApiCode apiCode) {
        return new CapabilityRouteRule(method, path, null, RouteMatchType.EXACT, apiCode.getCode());
    }

    private CapabilityRouteRule prefix(HttpMethod method, String path, ApiCode apiCode) {
        return new CapabilityRouteRule(method, path, null, RouteMatchType.PREFIX, apiCode.getCode());
    }

    private CapabilityRouteRule suffix(HttpMethod method, String path, String suffix, ApiCode apiCode) {
        return new CapabilityRouteRule(method, path, suffix, RouteMatchType.SUFFIX, apiCode.getCode());
    }
}
