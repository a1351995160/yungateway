---
title: WPS Yundoc Admin User Management RBAC Plan
type: feat
status: implemented
date: 2026-05-28
origin: user request
---

# WPS Yundoc Admin User Management RBAC Plan

## Problem Frame

The admin console needs a backend authority for "who can enter the console" and "who can configure user permissions." The current backend has a single configured admin account and a shared `ADMIN` token role. That works for an MVP login, but it cannot support a user management page, role assignment, disabled users, or safe frontend gating.

This plan keeps the super administrator in configuration, as requested. Database-backed admin users are managed by the configured super administrator only. The frontend may hide buttons for experience, but the backend remains the authorization boundary.

## Scope

In scope:

- Keep the configured account under `yundoc.admin-auth` as the only `SUPER_ADMIN`.
- Add database-backed admin users for day-to-day console access.
- Add roles: `SYSTEM_ADMIN`, `AUDITOR`, `SUPPORT`.
- Add active/disabled status checks for database-backed users.
- Add `/api/v1/admin/me` for the frontend shell.
- Add `/api/v1/admin/users` management APIs guarded by `SUPER_ADMIN`.
- Avoid returning password digests, salts, JWT secrets, or other sensitive material.

Out of scope for this iteration:

- Enterprise SSO.
- Password reset email/invitation delivery.
- Full audit log persistence.
- Fine-grained endpoint permissions for every existing business-system API.
- Moving admin JWT from bearer token to HttpOnly cookie.

## API Design

Use the existing response envelope: `ApiResponse.success(data, "unknown")` and `YundocErrorCode` failures.

Endpoints:

- `GET /api/v1/admin/me`
  - Returns the current admin principal: `username`, `displayName`, `role`, `status`, `superAdmin`, `lastLoginAt`.
  - Available to any valid admin token.

- `GET /api/v1/admin/users?keyword=&status=&role=&page=1&pageSize=20`
  - Super admin only.
  - Returns `items` and `hasMore`.
  - Does not expose login digests, salts, or password data.

- `POST /api/v1/admin/users`
  - Super admin only.
  - Creates a database-backed admin user with an initial password.
  - Request fields: `username`, `displayName`, `role`, `initialPassword`.
  - Allowed roles exclude `SUPER_ADMIN`.

- `PATCH /api/v1/admin/users/{username}`
  - Super admin only.
  - Updates `displayName`, `role`, `status`.
  - Cannot update the configured super admin.
  - Cannot assign `SUPER_ADMIN`.

## Security Design

- The configured admin account is always `SUPER_ADMIN`; it is authenticated from `application-*.yml`, not the database.
- Database users are authenticated by username and stored password digest.
- JWT payload gains `sub`, `role`, `typ=admin-jwt`, `iat`, and `exp`; validation rejects non-admin token types and missing/invalid roles.
- User management write APIs call a backend `requireSuperAdmin` check; frontend controls are only UX.
- Disabled database users cannot log in and existing tokens should fail when the backend validates the principal against the database.
- Initial passwords are accepted only as write-only request fields and are never returned.
- Use parameterized MyBatis mappings and explicit column lists.

## Frontend Contract

The frontend user management page should use user-facing Chinese labels:

- `username` -> 登录账号
- `displayName` -> 用户姓名
- `role` -> 角色
- `status` -> 状态
- `lastLoginAt` -> 最近登录时间
- `SYSTEM_ADMIN` -> 系统管理员
- `AUDITOR` -> 只读审计员
- `SUPPORT` -> 接入支持人员
- `ENABLED` -> 启用
- `DISABLED` -> 停用

The frontend should not display backend implementation fields such as digest, salt, algorithm, or token internals.

## Implementation Units

### U1. Admin Principal And JWT Roles

Files:

- `src/main/java/com/wps/yundoc/adminauth/application/AdminPrincipal.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminRole.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminStatus.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminJwtService.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminAuthService.java`
- `src/main/java/com/wps/yundoc/adminauth/infrastructure/AdminAuthInterceptor.java`

Tests:

- `src/test/java/com/wps/yundoc/adminauth/api/AdminAuthControllerTest.java`
- `src/test/java/com/wps/yundoc/adminauth/infrastructure/AdminAuthInterceptorTest.java`

Scenarios:

- Configured admin login returns a token with `SUPER_ADMIN`.
- Wrong password still returns 401.
- Invalid role or token type is rejected.
- Interceptor stores the authenticated principal for downstream controllers.

### U2. Database-Backed Admin Users

Files:

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/schema-clean.sql`
- `src/main/java/com/wps/yundoc/adminuser/infrastructure/AdminUserPO.java`
- `src/main/java/com/wps/yundoc/adminuser/infrastructure/AdminUserMapper.java`
- `src/main/resources/mapper/adminuser/AdminUserMapper.xml`

Tests:

- `src/test/java/com/wps/yundoc/adminuser/infrastructure/AdminUserMapperTest.java`

Scenarios:

- Insert/select/update uses explicit columns.
- List supports keyword/status/role and bounded pagination.
- Sensitive password digest fields are kept in persistence only.

### U3. User Management Service And API

Files:

- `src/main/java/com/wps/yundoc/adminuser/application/AdminUserService.java`
- `src/main/java/com/wps/yundoc/adminuser/api/AdminMeController.java`
- `src/main/java/com/wps/yundoc/adminuser/api/AdminUserController.java`
- request/response DTOs in `src/main/java/com/wps/yundoc/adminuser/api/`

Tests:

- `src/test/java/com/wps/yundoc/adminuser/api/AdminUserControllerTest.java`
- `src/test/java/com/wps/yundoc/adminuser/application/AdminUserServiceTest.java`

Scenarios:

- Super admin can create users.
- Super admin can list users without password material.
- Super admin can update role/status.
- Non-super admin cannot create/update/list managed users.
- `SUPER_ADMIN` cannot be assigned to database users.
- Disabled users cannot log in or keep using existing tokens.

### U4. Error Codes And Validation

Files:

- `src/main/java/com/wps/yundoc/common/error/YundocErrorCode.java`

Scenarios:

- Duplicate username returns validation failure or conflict-style code.
- Forbidden role changes return 403.
- User not found returns a stable user-readable error.

## Sequencing

1. Add role/status/principal types and adjust admin JWT issue/validate.
2. Add `admin_user` schema and MyBatis mapper.
3. Make login support config super admin first, then database users.
4. Add `/me` and `/users` APIs.
5. Add tests for super-admin-only mutations and disabled-user rejection.
6. Run focused Maven tests, then broaden if failures indicate shared behavior.

## Implementation Status

Implemented on 2026-05-28.

Added backend implementation:

- `src/main/java/com/wps/yundoc/adminauth/application/AdminPrincipal.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminRole.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminStatus.java`
- `src/main/java/com/wps/yundoc/adminuser/`
- `src/main/resources/mapper/adminuser/AdminUserMapper.xml`
- `src/test/java/com/wps/yundoc/adminuser/api/AdminUserControllerTest.java`

Updated backend implementation:

- `src/main/java/com/wps/yundoc/adminauth/application/AdminAuthService.java`
- `src/main/java/com/wps/yundoc/adminauth/application/AdminJwtService.java`
- `src/main/java/com/wps/yundoc/adminauth/infrastructure/AdminAuthInterceptor.java`
- `src/main/java/com/wps/yundoc/common/error/YundocErrorCode.java`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/schema-clean.sql`

Verification:

```powershell
.\mvnw.cmd test
```

Result: `104 tests, 0 failures, 0 errors`.
