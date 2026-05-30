package com.wps.yundoc.businesssystem.api;

import com.wps.yundoc.businesssystem.domain.ApiCode;
import com.wps.yundoc.businesssystem.domain.WpsIdentityType;

public class ApiPermissionDefinitionResponse {

    private static final String APP_PREVIEW_PREFIX = "app-preview";
    private static final String DELETE_OPERATION = "delete";
    private static final String DOWNLOAD_OPERATION = "download";
    private static final String RISK_HIGH = "HIGH";
    private static final String RISK_LOW = "LOW";
    private static final String RISK_MEDIUM = "MEDIUM";

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
        if (apiCode.getCode().startsWith(APP_PREVIEW_PREFIX)) {
            return "Allows app identity to create preview resources.";
        }
        if (isHighRisk(apiCode)) {
            return "Allows user identity to perform higher risk file operations.";
        }
        return "Allows user identity to perform file or folder operations.";
    }

    private static String riskLevel(ApiCode apiCode) {
        if (isHighRisk(apiCode)) {
            return RISK_HIGH;
        }
        if (WpsIdentityType.USER == apiCode.getIdentityType()) {
            return RISK_MEDIUM;
        }
        return RISK_LOW;
    }

    private static boolean isHighRisk(ApiCode apiCode) {
        return apiCode.getCode().contains(DELETE_OPERATION) || apiCode.getCode().contains(DOWNLOAD_OPERATION);
    }
}
