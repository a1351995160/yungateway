package com.wps.yundoc.credential.application;

import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import com.wps.yundoc.credential.domain.OAuthState;
import com.wps.yundoc.credential.domain.WpsUserToken;
import com.wps.yundoc.credential.infrastructure.LocalOAuthStateCache;
import com.wps.yundoc.credential.infrastructure.LocalWpsUserTokenCache;
import com.wps.yundoc.credential.infrastructure.WpsUserAuthorizationProperties;
import com.wps.yundoc.wpsclient.application.WpsAuthorizationClient;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WpsUserAuthorizationService {

    private final LocalWpsUserTokenCache tokenCache;
    private final LocalOAuthStateCache stateCache;
    private final WpsAuthorizationClient authorizationClient;
    private final WpsUserAuthorizationProperties properties;

    public WpsUserAuthorizationService(
            LocalWpsUserTokenCache tokenCache,
            LocalOAuthStateCache stateCache,
            WpsAuthorizationClient authorizationClient,
            WpsUserAuthorizationProperties properties) {
        this.tokenCache = tokenCache;
        this.stateCache = stateCache;
        this.authorizationClient = authorizationClient;
        this.properties = properties;
    }

    public WpsUserToken requireUserToken(String userId, String businessSystemId) {
        return tokenCache.get(userId).orElseThrow(() -> reauthRequired(userId, businessSystemId));
    }

    public void handleCallback(String code, String stateValue) {
        OAuthState state = validState(stateValue);
        WpsUserToken token = authorizationClient.exchangeCode(code);
        tokenCache.put(state.getUserId(), token);
    }

    private OAuthState validState(String stateValue) {
        return stateCache.take(stateValue)
                .orElseThrow(() -> new YundocException(YundocErrorCode.VALIDATION_FAILED));
    }

    private YundocException reauthRequired(String userId, String businessSystemId) {
        String state = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(properties.getStateTtl());
        stateCache.put(new OAuthState(state, userId, businessSystemId, expiresAt));
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("authorizeUrl", authorizationClient.authorizeUrl(state));
        details.put("expiresIn", Long.valueOf(properties.getStateTtl().getSeconds()));
        return new YundocException(YundocErrorCode.REAUTH_REQUIRED, details);
    }
}
