package com.wps.yundoc.adminauth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "yundoc.admin-auth")
public class AdminAuthProperties {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Pattern(regexp = "^[a-fA-F0-9]{64}$")
    private String loginDigest;

    @NotBlank
    @Size(max = 64)
    private String loginSalt;

    @NotBlank
    @Size(max = 32)
    private String loginAlgorithm = "HMAC-SHA256";

    @NotBlank
    @Size(min = 32, max = 256)
    private String jwtSecret;

    @Min(300)
    @Max(86400)
    private long jwtTtlSeconds = 1800;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginDigest() {
        return loginDigest;
    }

    public void setLoginDigest(String loginDigest) {
        this.loginDigest = loginDigest;
    }

    public String getLoginSalt() {
        return loginSalt;
    }

    public void setLoginSalt(String loginSalt) {
        this.loginSalt = loginSalt;
    }

    public String getLoginAlgorithm() {
        return loginAlgorithm;
    }

    public void setLoginAlgorithm(String loginAlgorithm) {
        this.loginAlgorithm = loginAlgorithm;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtTtlSeconds() {
        return jwtTtlSeconds;
    }

    public void setJwtTtlSeconds(long jwtTtlSeconds) {
        this.jwtTtlSeconds = jwtTtlSeconds;
    }
}
