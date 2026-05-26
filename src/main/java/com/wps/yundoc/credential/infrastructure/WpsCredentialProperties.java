package com.wps.yundoc.credential.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "yundoc.wps-credential")
public class WpsCredentialProperties {

    private String appToken;
    private Duration appTokenTtl = Duration.ofMinutes(30);
    private Duration refreshSkew = Duration.ofMinutes(5);

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public Duration getAppTokenTtl() {
        return appTokenTtl;
    }

    public void setAppTokenTtl(Duration appTokenTtl) {
        this.appTokenTtl = appTokenTtl;
    }

    public Duration getRefreshSkew() {
        return refreshSkew;
    }

    public void setRefreshSkew(Duration refreshSkew) {
        this.refreshSkew = refreshSkew;
    }

    public boolean hasAppToken() {
        if (appToken == null) {
            return false;
        }
        return !appToken.trim().isEmpty();
    }
}
