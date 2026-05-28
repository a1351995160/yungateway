package com.wps.yundoc.adminuser.application;

import com.wps.yundoc.adminauth.application.AdminRole;
import com.wps.yundoc.adminauth.application.AdminStatus;
import com.wps.yundoc.adminauth.infrastructure.AdminAuthProperties;
import com.wps.yundoc.adminuser.api.AdminUserCreateRequest;
import com.wps.yundoc.adminuser.api.AdminUserListRequest;
import com.wps.yundoc.adminuser.api.AdminUserListResponse;
import com.wps.yundoc.adminuser.api.AdminUserResponse;
import com.wps.yundoc.adminuser.api.AdminUserUpdateRequest;
import com.wps.yundoc.adminuser.infrastructure.AdminUserMapper;
import com.wps.yundoc.adminuser.infrastructure.AdminUserPO;
import com.wps.yundoc.auth.application.ClientSecretDigest;
import com.wps.yundoc.auth.application.ClientSecretDigestService;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminAuthProperties adminAuthProperties;
    private final ClientSecretDigestService digestService;

    public AdminUserService(
            AdminUserMapper adminUserMapper,
            AdminAuthProperties adminAuthProperties,
            ClientSecretDigestService digestService) {
        this.adminUserMapper = adminUserMapper;
        this.adminAuthProperties = adminAuthProperties;
        this.digestService = digestService;
    }

    public AdminUserListResponse list(AdminUserListRequest request) {
        int limit = request.getPageSize() + 1;
        int offset = (request.getPage() - 1) * request.getPageSize();
        List<AdminUserPO> users = adminUserMapper.selectPage(
                request.getKeyword(),
                request.getStatus(),
                request.getRole(),
                limit,
                offset);
        boolean hasMore = users.size() > request.getPageSize();
        List<AdminUserResponse> items = new ArrayList<>();
        int itemCount = Math.min(users.size(), request.getPageSize());
        for (int index = 0; index < itemCount; index++) {
            items.add(toResponse(users.get(index)));
        }
        return new AdminUserListResponse(items, hasMore);
    }

    public AdminUserResponse create(AdminUserCreateRequest request) {
        assertDatabaseRole(request.getRole());
        assertNotConfiguredSuperAdmin(request.getUsername());
        LocalDateTime now = LocalDateTime.now();
        ClientSecretDigest digest = digestService.digestNew(request.getInitialPassword());
        AdminUserPO adminUser = new AdminUserPO();
        adminUser.setUsername(request.getUsername());
        adminUser.setDisplayName(request.getDisplayName());
        adminUser.setRole(request.getRole().name());
        adminUser.setStatus(AdminStatus.ENABLED.name());
        adminUser.setLoginDigest(digest.getDigest());
        adminUser.setLoginSalt(digest.getSalt());
        adminUser.setLoginAlgorithm(digest.getAlgorithm());
        adminUser.setCreatedAt(now);
        adminUser.setUpdatedAt(now);
        try {
            adminUserMapper.insert(adminUser);
        } catch (DuplicateKeyException ex) {
            throw new YundocException(YundocErrorCode.VALIDATION_FAILED, "Admin user already exists", ex);
        }
        return toResponse(adminUserMapper.selectByUsername(request.getUsername()));
    }

    public AdminUserResponse update(String username, AdminUserUpdateRequest request) {
        assertDatabaseRole(request.getRole());
        assertNotConfiguredSuperAdmin(username);
        AdminUserPO existing = adminUserMapper.selectByUsername(username);
        if (existing == null) {
            throw new YundocException(YundocErrorCode.ADMIN_USER_NOT_FOUND);
        }
        adminUserMapper.updateProfile(
                username,
                request.getDisplayName(),
                request.getRole().name(),
                request.getStatus().name(),
                LocalDateTime.now());
        return toResponse(adminUserMapper.selectByUsername(username));
    }

    private void assertDatabaseRole(AdminRole role) {
        if (role == AdminRole.SUPER_ADMIN) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }

    private void assertNotConfiguredSuperAdmin(String username) {
        if (adminAuthProperties.getUsername().equals(username)) {
            throw new YundocException(YundocErrorCode.ADMIN_PERMISSION_DENIED);
        }
    }

    private AdminUserResponse toResponse(AdminUserPO user) {
        return new AdminUserResponse(
                user.getUsername(),
                user.getDisplayName(),
                AdminRole.valueOf(user.getRole()),
                AdminStatus.valueOf(user.getStatus()),
                false,
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
