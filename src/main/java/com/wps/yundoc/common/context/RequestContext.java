package com.wps.yundoc.common.context;

public class RequestContext {

    private final String requestId;
    private final String businessSystemId;
    private final String clientId;
    private final String jti;
    private final Integer tokenVersion;
    private final Integer permissionVersion;
    private final String apiCode;
    private final String userId;

    private RequestContext(Builder builder) {
        this.requestId = builder.requestId;
        this.businessSystemId = builder.businessSystemId;
        this.clientId = builder.clientId;
        this.jti = builder.jti;
        this.tokenVersion = builder.tokenVersion;
        this.permissionVersion = builder.permissionVersion;
        this.apiCode = builder.apiCode;
        this.userId = builder.userId;
    }

    public static Builder builder(String requestId) {
        return new Builder(requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getJti() {
        return jti;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public Integer getPermissionVersion() {
        return permissionVersion;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getUserId() {
        return userId;
    }

    public static class Builder {

        private final String requestId;
        private String businessSystemId;
        private String clientId;
        private String jti;
        private Integer tokenVersion;
        private Integer permissionVersion;
        private String apiCode;
        private String userId;

        private Builder(String requestId) {
            this.requestId = requestId;
        }

        public Builder businessSystemId(String value) {
            this.businessSystemId = value;
            return this;
        }

        public Builder clientId(String value) {
            this.clientId = value;
            return this;
        }

        public Builder jti(String value) {
            this.jti = value;
            return this;
        }

        public Builder tokenVersion(Integer value) {
            this.tokenVersion = value;
            return this;
        }

        public Builder permissionVersion(Integer value) {
            this.permissionVersion = value;
            return this;
        }

        public Builder apiCode(String value) {
            this.apiCode = value;
            return this;
        }

        public Builder userId(String value) {
            this.userId = value;
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }
}
