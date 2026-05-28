package com.wps.yundoc.adminauth.application;

import com.wps.yundoc.adminauth.api.AdminLoginRequest;
import com.wps.yundoc.adminauth.infrastructure.AdminAuthProperties;
import com.wps.yundoc.adminuser.infrastructure.AdminUserMapper;
import com.wps.yundoc.adminuser.infrastructure.AdminUserPO;
import com.wps.yundoc.auth.application.ClientSecretDigestService;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminAuthProperties properties;
    private final AdminJwtService adminJwtService;
    private final ClientSecretDigestService digestService;
    private final AdminUserMapper adminUserMapper;

    public AdminAuthService(
            AdminAuthProperties properties,
            AdminJwtService adminJwtService,
            ClientSecretDigestService digestService,
            AdminUserMapper adminUserMapper) {
        this.properties = properties;
        this.adminJwtService = adminJwtService;
        this.digestService = digestService;
        this.adminUserMapper = adminUserMapper;
    }

    public AdminJwt login(AdminLoginRequest request) {
        if (properties.getUsername().equals(request.getUsername())) {
            return loginConfiguredSuperAdmin(request);
        }
        return loginDatabaseUser(request);
    }

    public AdminPrincipal requireAdmin(String authorizationHeader) {
        AdminPrincipal tokenPrincipal = adminJwtService.validate(authorizationHeader);
        if (tokenPrincipal.isSuperAdmin()) {
            return requireConfiguredSuperAdmin(tokenPrincipal);
        }
        return requireEnabledDatabaseUser(tokenPrincipal);
    }

    public AdminPrincipal requireSuperAdmin(String authorizationHeader) {
        AdminPrincipal principal = requireAdmin(authorizationHeader);
        if (!principal.isSuperAdmin()) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
        return principal;
    }

    private AdminJwt loginConfiguredSuperAdmin(AdminLoginRequest request) {
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
        return adminJwtService.issue(request.getUsername(), AdminRole.SUPER_ADMIN);
    }

    private AdminJwt loginDatabaseUser(AdminLoginRequest request) {
        AdminUserPO user = adminUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        if (!AdminStatus.ENABLED.name().equals(user.getStatus())) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        validateDatabasePassword(request.getPassword(), user);
        LocalDateTime lastLoginAt = LocalDateTime.now();
        adminUserMapper.updateLastLoginAt(user.getUsername(), lastLoginAt);
        return adminJwtService.issue(user.getUsername(), AdminRole.valueOf(user.getRole()));
    }

    private AdminPrincipal requireConfiguredSuperAdmin(AdminPrincipal tokenPrincipal) {
        if (!properties.getUsername().equals(tokenPrincipal.getUsername())) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        return AdminPrincipal.superAdmin(properties.getUsername());
    }

    private AdminPrincipal requireEnabledDatabaseUser(AdminPrincipal tokenPrincipal) {
        AdminUserPO user = adminUserMapper.selectByUsername(tokenPrincipal.getUsername());
        if (user == null || !AdminStatus.ENABLED.name().equals(user.getStatus())) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        if (!user.getRole().equals(tokenPrincipal.getRole().name())) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
        return new AdminPrincipal(
                user.getUsername(),
                user.getDisplayName(),
                AdminRole.valueOf(user.getRole()),
                AdminStatus.valueOf(user.getStatus()),
                user.getLastLoginAt());
    }

    private void validateUsername(String username) {
        if (!properties.getUsername().equals(username)) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private void validatePassword(String password) {
        boolean matched = digestService.matches(
                password,
                properties.getLoginSalt(),
                properties.getLoginAlgorithm(),
                properties.getLoginDigest());
        if (!matched) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }

    private void validateDatabasePassword(String password, AdminUserPO user) {
        boolean matched = digestService.matches(
                password,
                user.getLoginSalt(),
                user.getLoginAlgorithm(),
                user.getLoginDigest());
        if (!matched) {
            throw new YundocException(YundocErrorCode.TOKEN_INVALID);
        }
    }
}
