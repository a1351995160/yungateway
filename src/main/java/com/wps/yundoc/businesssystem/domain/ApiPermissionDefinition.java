package com.wps.yundoc.businesssystem.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ApiPermissionDefinition {

    private static final List<ApiCode> ALL_API_CODES =
            Collections.unmodifiableList(Arrays.asList(ApiCode.values()));

    private ApiPermissionDefinition() {
    }

    public static List<ApiCode> all() {
        return ALL_API_CODES;
    }

    public static List<String> allCodes() {
        return ALL_API_CODES.stream()
                .map(ApiCode::getCode)
                .collect(Collectors.toList());
    }

    public static Optional<ApiCode> find(String code) {
        return ApiCode.fromCode(code);
    }

    public static boolean exists(String code) {
        return find(code).isPresent();
    }

    public static WpsIdentityType identityTypeOf(String code) {
        return find(code)
                .map(ApiCode::getIdentityType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown api code: " + code));
    }
}
