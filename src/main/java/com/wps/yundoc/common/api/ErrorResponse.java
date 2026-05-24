package com.wps.yundoc.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final String upstreamCategory;

    private ErrorResponse(String code, String message, String upstreamCategory) {
        this.code = code;
        this.message = message;
        this.upstreamCategory = upstreamCategory;
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, String upstreamCategory) {
        return new ErrorResponse(code, message, upstreamCategory);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getUpstreamCategory() {
        return upstreamCategory;
    }
}

