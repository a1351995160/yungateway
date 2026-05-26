package com.wps.yundoc.credential.application;

import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import com.wps.yundoc.credential.domain.WpsCredential;
import com.wps.yundoc.credential.infrastructure.LocalWpsTokenCache;
import com.wps.yundoc.credential.infrastructure.WpsCredentialProperties;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class WpsCredentialService {

    private final LocalWpsTokenCache tokenCache;
    private final WpsCredentialProperties properties;

    public WpsCredentialService(LocalWpsTokenCache tokenCache, WpsCredentialProperties properties) {
        this.tokenCache = tokenCache;
        this.properties = properties;
    }

    public WpsCredential appCredential() {
        return tokenCache.get().orElseGet(this::createAppCredential);
    }

    private WpsCredential createAppCredential() {
        if (!properties.hasAppToken()) {
            throw new YundocException(YundocErrorCode.REAUTH_REQUIRED);
        }
        WpsCredential credential = new WpsCredential(properties.getAppToken(), expiresAt());
        tokenCache.put(credential);
        return credential;
    }

    private OffsetDateTime expiresAt() {
        return OffsetDateTime.now().plus(properties.getAppTokenTtl());
    }
}
