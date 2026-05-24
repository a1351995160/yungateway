package com.wps.yundoc.common.error;

public class YundocException extends RuntimeException {

    private final YundocErrorCode errorCode;
    private final String upstreamCategory;

    public YundocException(YundocErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null, null);
    }

    public YundocException(YundocErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public YundocException(YundocErrorCode errorCode, String message, String upstreamCategory) {
        this(errorCode, message, upstreamCategory, null);
    }

    public YundocException(YundocErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    private YundocException(YundocErrorCode errorCode, String message, String upstreamCategory, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.upstreamCategory = upstreamCategory;
    }

    public YundocErrorCode getErrorCode() {
        return errorCode;
    }

    public String getUpstreamCategory() {
        return upstreamCategory;
    }
}

