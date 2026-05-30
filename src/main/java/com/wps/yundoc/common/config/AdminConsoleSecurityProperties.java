package com.wps.yundoc.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "yundoc.admin-console")
public class AdminConsoleSecurityProperties {

    private static final String PATH_SEPARATOR = "/";
    private static final String API_SEGMENT = "api";
    private static final String VERSION_SEGMENT = "v1";
    private static final String ADMIN_SEGMENT = "admin";
    private static final String AUTH_SEGMENT = "auth";
    private static final String LOGIN_SEGMENT = "login";

    private List<String> allowedOrigins = new ArrayList<>();
    private boolean secureCookies = true;
    private String adminCookiePath = path(API_SEGMENT, VERSION_SEGMENT, ADMIN_SEGMENT);
    private String adminApiPrefix = adminCookiePath + PATH_SEPARATOR;
    private String adminLoginPath = path(API_SEGMENT, VERSION_SEGMENT, ADMIN_SEGMENT, AUTH_SEGMENT, LOGIN_SEGMENT);

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public boolean isSecureCookies() {
        return secureCookies;
    }

    public void setSecureCookies(boolean secureCookies) {
        this.secureCookies = secureCookies;
    }

    public String getAdminCookiePath() {
        return adminCookiePath;
    }

    public void setAdminCookiePath(String adminCookiePath) {
        this.adminCookiePath = adminCookiePath;
    }

    public String getAdminApiPrefix() {
        return adminApiPrefix;
    }

    public void setAdminApiPrefix(String adminApiPrefix) {
        this.adminApiPrefix = adminApiPrefix;
    }

    public String getAdminLoginPath() {
        return adminLoginPath;
    }

    public void setAdminLoginPath(String adminLoginPath) {
        this.adminLoginPath = adminLoginPath;
    }

    private static String path(String... segments) {
        return PATH_SEPARATOR + String.join(PATH_SEPARATOR, segments);
    }
}
