package com.wps.yundoc.common.error;

import java.util.Collections;
import java.util.Map;

public class YundocException extends RuntimeException {

    private final YundocErrorCode errorCode;
    private final String upstreamCategory;
    private final Map<String, Object> details;

    public YundocException(YundocErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null, null, null);
    }

    public YundocException(YundocErrorCode errorCode, String message) {
        this(errorCode, message, null, null, null);
    }

    public YundocException(YundocErrorCode errorCode, String message, String upstreamCategory) {
        this(errorCode, message, upstreamCategory, null, null);
    }

    public YundocException(YundocErrorCode errorCode, String message, Map<String, Object> details) {
        this(errorCode, message, null, details, null);
    }

    public YundocException(YundocErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, null, cause);
    }

    private YundocException(
            YundocErrorCode errorCode,
            String message,
            String upstreamCategory,
            Map<String, Object> details,
            Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.upstreamCategory = upstreamCategory;
        this.details = immutableDetails(details);
    }

    public YundocErrorCode getErrorCode() {
        return errorCode;
    }

    public String getUpstreamCategory() {
        return upstreamCategory;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    private Map<String, Object> immutableDetails(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableMap(value);
    }
}

