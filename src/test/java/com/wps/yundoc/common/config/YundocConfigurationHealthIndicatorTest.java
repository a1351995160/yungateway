package com.wps.yundoc.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;

class YundocConfigurationHealthIndicatorTest {

    @Test
    void healthExposesReadinessFlags() {
        YundocReadinessProperties properties = new YundocReadinessProperties();
        properties.setRedisRequired(false);
        properties.setTdsqlRequired(true);

        Health health = new YundocConfigurationHealthIndicator(properties).health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("tdsqlRequired", true);
        assertThat(health.getDetails()).containsEntry("redisRequired", false);
    }
}

