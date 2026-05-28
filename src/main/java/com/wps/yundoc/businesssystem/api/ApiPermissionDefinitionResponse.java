package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.businesssystem.domain.ApiCode;

public class ApiPermissionDefinitionResponse {

    private final String apiCode;
    private final String identityType;
    private final String displayName;
    private final String description;
    private final String riskLevel;

    public ApiPermissionDefinitionResponse(
            String apiCode,
            String identityType,
            String displayName,
            String description,
            String riskLevel) {
        this.apiCode = apiCode;
        this.identityType = identityType;
        this.displayName = displayName;
        this.description = description;
        this.riskLevel = riskLevel;
    }

    public static ApiPermissionDefinitionResponse of(ApiCode apiCode) {
        return new ApiPermissionDefinitionResponse(
                apiCode.getCode(),
                apiCode.getIdentityType().name(),
                displayName(apiCode),
                description(apiCode),
                riskLevel(apiCode));
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getIdentityType() {
        return identityType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    private static String displayName(ApiCode apiCode) {
        String code = apiCode.getCode().replace(':', ' ').replace('-', ' ');
        return Character.toUpperCase(code.charAt(0)) + code.substring(1);
    }

    private static String description(ApiCode apiCode) {
        if (apiCode.getCode().startsWith("app-preview")) {
            return "Allows app identity to create preview resources.";
        }
        if (apiCode.getCode().contains("download") || apiCode.getCode().contains("delete")) {
            return "Allows user identity to perform higher risk file operations.";
        }
        return "Allows user identity to perform file or folder operations.";
    }

    private static String riskLevel(ApiCode apiCode) {
        if (apiCode.getCode().contains("delete") || apiCode.getCode().contains("download")) {
            return "HIGH";
        }
        if (apiCode.getIdentityType().name().equals("USER")) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
