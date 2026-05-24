package com.wps.yundoc.common.context;

import java.util.Collections;
import java.util.Set;

public class RequestContext {

    private final String requestId;
    private final String businessSystemId;
    private final String clientId;
    private final String wpsAuthMode;
    private final String wpsCompanyId;
    private final String operatorId;
    private final String jti;
    private final String secretVersion;
    private final String scopeVersion;
    private final String policyVersion;
    private final Set<String> scopes;

    private RequestContext(Builder builder) {
        this.requestId = builder.requestId;
        this.businessSystemId = builder.businessSystemId;
        this.clientId = builder.clientId;
        this.wpsAuthMode = builder.wpsAuthMode;
        this.wpsCompanyId = builder.wpsCompanyId;
        this.operatorId = builder.operatorId;
        this.jti = builder.jti;
        this.secretVersion = builder.secretVersion;
        this.scopeVersion = builder.scopeVersion;
        this.policyVersion = builder.policyVersion;
        this.scopes = builder.scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(builder.scopes);
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

    public String getWpsAuthMode() {
        return wpsAuthMode;
    }

    public String getWpsCompanyId() {
        return wpsCompanyId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getJti() {
        return jti;
    }

    public String getSecretVersion() {
        return secretVersion;
    }

    public String getScopeVersion() {
        return scopeVersion;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public static class Builder {

        private final String requestId;
        private String businessSystemId;
        private String clientId;
        private String wpsAuthMode;
        private String wpsCompanyId;
        private String operatorId;
        private String jti;
        private String secretVersion;
        private String scopeVersion;
        private String policyVersion;
        private Set<String> scopes;

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

        public Builder wpsAuthMode(String value) {
            this.wpsAuthMode = value;
            return this;
        }

        public Builder wpsCompanyId(String value) {
            this.wpsCompanyId = value;
            return this;
        }

        public Builder operatorId(String value) {
            this.operatorId = value;
            return this;
        }

        public Builder jti(String value) {
            this.jti = value;
            return this;
        }

        public Builder secretVersion(String value) {
            this.secretVersion = value;
            return this;
        }

        public Builder scopeVersion(String value) {
            this.scopeVersion = value;
            return this;
        }

        public Builder policyVersion(String value) {
            this.policyVersion = value;
            return this;
        }

        public Builder scopes(Set<String> value) {
            this.scopes = value;
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }
}

