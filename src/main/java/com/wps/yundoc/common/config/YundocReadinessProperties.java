package com.wps.yundoc.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "yundoc.readiness")
public class YundocReadinessProperties {

    private boolean tdsqlRequired = true;
    private boolean redisRequired = true;

    public boolean isTdsqlRequired() {
        return tdsqlRequired;
    }

    public void setTdsqlRequired(boolean tdsqlRequired) {
        this.tdsqlRequired = tdsqlRequired;
    }

    public boolean isRedisRequired() {
        return redisRequired;
    }

    public void setRedisRequired(boolean redisRequired) {
        this.redisRequired = redisRequired;
    }
}

