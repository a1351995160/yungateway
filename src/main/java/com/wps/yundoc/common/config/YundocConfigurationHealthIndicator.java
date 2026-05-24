package com.wps.yundoc.common.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("yundocConfiguration")
public class YundocConfigurationHealthIndicator implements HealthIndicator {

    private final YundocReadinessProperties readinessProperties;

    public YundocConfigurationHealthIndicator(YundocReadinessProperties readinessProperties) {
        this.readinessProperties = readinessProperties;
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("tdsqlRequired", readinessProperties.isTdsqlRequired())
                .withDetail("redisRequired", readinessProperties.isRedisRequired())
                .build();
    }
}

