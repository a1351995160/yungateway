package com.wps.yundoc.common.error;

public enum YundocErrorCode {
    YUNDOC_AUTH_REQUIRED(401, "Authentication is required"),
    YUNDOC_TOKEN_INVALID(401, "Token is invalid"),
    YUNDOC_PERMISSION_DENIED(403, "Permission denied"),
    YUNDOC_SCOPE_DENIED(403, "Scope denied"),
    YUNDOC_REAUTH_REQUIRED(401, "WPS user authorization is required"),
    YUNDOC_VALIDATION_FAILED(400, "Request validation failed"),
    YUNDOC_IDEMPOTENCY_CONFLICT(409, "Idempotency conflict"),
    YUNDOC_WPS_UPSTREAM_ERROR(502, "WPS upstream error"),
    YUNDOC_INTERNAL_ERROR(500, "Internal server error");

    private final int httpStatus;
    private final String defaultMessage;

    YundocErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

