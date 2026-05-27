package com.wps.yundoc.credential.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "yundoc.wps-user-authorization")
public class WpsUserAuthorizationProperties {

    private Duration stateTtl = Duration.ofMinutes(5);

    public Duration getStateTtl() {
        return stateTtl;
    }

    public void setStateTtl(Duration stateTtl) {
        this.stateTtl = stateTtl;
    }
}
