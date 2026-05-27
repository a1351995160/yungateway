package com.wps.yundoc.adminauth.application;

import com.wps.yundoc.adminauth.api.AdminLoginRequest;
import com.wps.yundoc.adminauth.infrastructure.AdminAuthProperties;
import com.wps.yundoc.auth.application.ClientSecretDigestService;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminAuthProperties properties;
    private final AdminJwtService adminJwtService;
    private final ClientSecretDigestService digestService;

    public AdminAuthService(
            AdminAuthProperties properties,
            AdminJwtService adminJwtService,
            ClientSecretDigestService digestService) {
        this.properties = properties;
        this.adminJwtService = adminJwtService;
        this.digestService = digestService;
    }

    public AdminJwt login(AdminLoginRequest request) {
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
        return adminJwtService.issue(request.getUsername());
    }

    public void requireAdmin(String authorizationHeader) {
        adminJwtService.validate(authorizationHeader);
    }

    private void validateUsername(String username) {
        if (!properties.getUsername().equals(username)) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private void validatePassword(String password) {
        boolean matched = digestService.matches(
                password,
                properties.getCredentialSalt(),
                properties.getCredentialAlgorithm(),
                properties.getCredentialDigest());
        if (!matched) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }
}
