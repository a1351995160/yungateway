package com.wps.yundoc.common.infrastructure;

import com.wps.yundoc.common.config.AdminConsoleSecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminConsoleCorsConfiguration implements WebMvcConfigurer {

    private static final String[] ADMIN_CONSOLE_METHODS = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};
    private static final String[] ADMIN_CONSOLE_HEADERS =
            {"Authorization", "Content-Type", "X-Request-Id", "X-CSRF-Token"};

    private final AdminConsoleSecurityProperties properties;

    public AdminConsoleCorsConfiguration(AdminConsoleSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (properties.getAllowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/api/v1/admin/**")
                .allowedOrigins(properties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(ADMIN_CONSOLE_METHODS)
                .allowedHeaders(ADMIN_CONSOLE_HEADERS)
                .allowCredentials(true)
                .maxAge(1800);
    }
}
