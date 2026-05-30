package com.wps.yundoc.auth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "yundoc.user-assertion")
public class UserAssertionProperties {

    @NotBlank
    @Size(min = 32, max = 256)
    private String secret;

    @NotBlank
    @Size(max = 32)
    private String keyId = "v1";

    private Duration timestampTolerance = Duration.ofMinutes(5);
    private int maxNonceCount = 10000;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public Duration getTimestampTolerance() {
        return timestampTolerance;
    }

    public void setTimestampTolerance(Duration timestampTolerance) {
        this.timestampTolerance = timestampTolerance;
    }

    public int getMaxNonceCount() {
        return maxNonceCount;
    }

    public void setMaxNonceCount(int maxNonceCount) {
        this.maxNonceCount = maxNonceCount;
    }
}
