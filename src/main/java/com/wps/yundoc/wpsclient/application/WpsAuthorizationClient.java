package com.wps.yundoc.wpsclient.application;

import com.wps.yundoc.credential.domain.OAuthState;
import com.wps.yundoc.credential.domain.WpsUserToken;

public interface WpsAuthorizationClient {

    String authorizeUrl(OAuthState state);

    WpsUserToken exchangeCode(String code, String userId);
}
