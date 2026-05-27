package com.wps.yundoc.credential.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "yundoc.wps-credential")
public class WpsCredentialProperties {

    private Duration refreshSkew = Duration.ofMinutes(5);

    public Duration getRefreshSkew() {
        return refreshSkew;
    }

    public void setRefreshSkew(Duration refreshSkew) {
        this.refreshSkew = refreshSkew;
    }
}
