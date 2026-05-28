# WPS Yundoc 管理后台用户管理与 RBAC 设计

状态：最新
日期：2026-05-28

## 1. 设计定位

管理后台需要回答两个问题：

1. 谁可以登录 WPS 云文档能力中转服务管理台。
2. 谁可以配置后台用户、业务系统、接口授权和密钥。

本设计把“超级管理员”保留在受控配置文件中，作为系统引导账号和最高权限账号。普通后台用户存储在数据库中，只能由配置中的超级管理员创建、停用和调整角色。前端可以根据角色隐藏入口和按钮，但后端始终是权限边界。

## 2. 角色模型

| 后端值 | 中文名称 | 用途 |
| --- | --- | --- |
| `SUPER_ADMIN` | 超级管理员 | 只来自配置文件。可以管理后台用户、业务系统、接口授权和密钥。 |
| `SYSTEM_ADMIN` | 系统管理员 | 数据库用户。用于日常管理业务系统和接口授权，不允许管理后台用户。 |
| `AUDITOR` | 只读审计员 | 数据库用户。用于查看后台数据，不应执行写操作。 |
| `SUPPORT` | 接入支持人员 | 数据库用户。用于查看业务系统和接入指南，辅助排查接入问题。 |

约束：

- `SUPER_ADMIN` 不写入 `admin_user` 表。
- `POST /api/v1/admin/users` 和 `PATCH /api/v1/admin/users/{username}` 不能创建或设置 `SUPER_ADMIN`。
- 只有 `SUPER_ADMIN` 可以管理后台用户。
- 当前阶段先实现用户管理 API 的超级管理员保护；更细粒度的业务系统写权限可以在后续按角色补齐。

## 3. 状态模型

| 后端值 | 中文名称 | 含义 |
| --- | --- | --- |
| `ENABLED` | 启用 | 用户可以登录，已有 token 在服务端校验时仍可通过。 |
| `DISABLED` | 停用 | 用户不能登录，已有 token 在服务端校验用户状态时失效。 |

## 4. 数据模型

新增表：`admin_user`

| 字段 | 类型 | 说明 | 是否返回前端 |
| --- | --- | --- | --- |
| `username` | `VARCHAR(64)` | 登录账号，主键 | 是 |
| `display_name` | `VARCHAR(128)` | 用户姓名/展示名 | 是 |
| `role` | `VARCHAR(32)` | `SYSTEM_ADMIN`、`AUDITOR`、`SUPPORT` | 是 |
| `status` | `VARCHAR(16)` | `ENABLED`、`DISABLED` | 是 |
| `login_digest` | `CHAR(64)` | 密码摘要 | 否 |
| `login_salt` | `VARCHAR(64)` | 密码盐 | 否 |
| `login_algorithm` | `VARCHAR(32)` | 摘要算法 | 否 |
| `last_login_at` | `DATETIME(3)` | 最近登录时间 | 是 |
| `created_at` | `DATETIME(3)` | 创建时间 | 是 |
| `updated_at` | `DATETIME(3)` | 更新时间 | 是 |

敏感字段不得出现在 API 响应、前端页面、日志、错误信息或文档示例中。

## 5. 认证设计

### 5.1 登录

接口：

```http
POST /api/v1/admin/auth/login
```

登录流程：

1. 如果用户名等于 `yundoc.admin-auth.username`，按配置中的摘要、盐和算法校验密码。
2. 配置账号校验成功后签发 `SUPER_ADMIN` admin JWT。
3. 如果不是配置账号，则查询 `admin_user` 表。
4. 数据库用户必须存在且状态为 `ENABLED`。
5. 数据库用户密码校验成功后，更新 `last_login_at` 并签发携带该用户角色的 admin JWT。

### 5.2 JWT 载荷

admin JWT 载荷包含：

```json
{
  "sub": "admin",
  "role": "SUPER_ADMIN",
  "iat": 1779960000,
  "exp": 1779961800,
  "typ": "admin-jwt"
}
```

校验规则：

- `typ` 必须是 `admin-jwt`。
- `role` 必须是合法后台角色。
- `sub` 不能为空。
- token 未过期。
- `SUPER_ADMIN` token 的 `sub` 必须等于配置用户名。
- 数据库用户 token 需要回查 `admin_user`，用户必须存在、启用，且角色与 token 中角色一致。

## 6. API 契约

所有接口沿用现有 `ApiResponse` 响应包裹。

### 6.1 当前登录用户

```http
GET /api/v1/admin/me
Authorization: Bearer <admin-jwt>
```

响应字段：

```json
{
  "success": true,
  "data": {
    "username": "admin",
    "displayName": "admin",
    "role": "SUPER_ADMIN",
    "status": "ENABLED",
    "superAdmin": true,
    "lastLoginAt": null,
    "createdAt": null,
    "updatedAt": null
  },
  "error": null,
  "requestId": "unknown",
  "pagination": null
}
```

### 6.2 后台用户列表

```http
GET /api/v1/admin/users?keyword=&status=&role=&page=1&pageSize=20
Authorization: Bearer <admin-jwt>
```

权限：仅 `SUPER_ADMIN`。

筛选：

- `keyword`：匹配登录账号或用户姓名。
- `status`：`ENABLED`、`DISABLED` 或空。
- `role`：`SYSTEM_ADMIN`、`AUDITOR`、`SUPPORT` 或空。
- `pageSize`：1 到 100。

### 6.3 创建后台用户

```http
POST /api/v1/admin/users
Authorization: Bearer <admin-jwt>
Content-Type: application/json

{
  "username": "support01",
  "displayName": "接入支持一组",
  "role": "SUPPORT",
  "initialPassword": "<initial-password>"
}
```

权限：仅 `SUPER_ADMIN`。

约束：

- `username` 只能使用字母、数字、下划线、点、短横线，长度 3 到 64。
- `displayName` 最长 128。
- `initialPassword` 长度 8 到 128，只作为写入字段，不返回。
- `role` 不能是 `SUPER_ADMIN`。
- `username` 不能等于配置中的超级管理员账号。

### 6.4 更新后台用户

```http
PATCH /api/v1/admin/users/{username}
Authorization: Bearer <admin-jwt>
Content-Type: application/json

{
  "displayName": "接入支持一组",
  "role": "SUPPORT",
  "status": "DISABLED"
}
```

权限：仅 `SUPER_ADMIN`。

约束：

- 不能更新配置中的超级管理员。
- 不能把数据库用户设置为 `SUPER_ADMIN`。
- 用户不存在时返回 `ADMIN_USER_NOT_FOUND`。

## 7. 错误码

| 错误码 | HTTP | 含义 |
| --- | --- | --- |
| `AUTH_REQUIRED` | 401 | 未携带认证信息。 |
| `TOKEN_INVALID` | 401 | token、用户名、密码、用户状态或 token 载荷无效。 |
| `ADMIN_PERMISSION_DENIED` | 403 | 当前管理员无权执行该操作。 |
| `ADMIN_USER_NOT_FOUND` | 404 | 后台用户不存在。 |
| `VALIDATION_FAILED` | 400 | 请求字段校验失败或用户名重复。 |

## 8. 前端页面设计要求

导航建议：

```text
业务系统
权限管理
用户管理
接入指南
```

`用户管理` 入口只对 `superAdmin=true` 的当前用户展示。后端仍会对接口做强制校验。

用户管理页必须包含：

- 搜索：登录账号、用户姓名。
- 筛选：角色、状态。
- 表格字段：登录账号、用户姓名、角色、状态、最近登录时间、更新时间、操作。
- 操作：新增用户、编辑角色、启用/停用。
- 危险提示：停用用户会使其无法登录，并使已有 token 在下次服务端校验时失效。

前端中文文案：

| 后端字段/值 | 前端文案 |
| --- | --- |
| `username` | 登录账号 |
| `displayName` | 用户姓名 |
| `role` | 角色 |
| `status` | 状态 |
| `lastLoginAt` | 最近登录时间 |
| `SYSTEM_ADMIN` | 系统管理员 |
| `AUDITOR` | 只读审计员 |
| `SUPPORT` | 接入支持人员 |
| `ENABLED` | 启用 |
| `DISABLED` | 停用 |

## 9. 已验证行为

当前后端测试覆盖：

- 配置超级管理员可以读取当前身份。
- 配置超级管理员可以创建数据库后台用户。
- 数据库后台用户可以登录并读取当前身份。
- 非超级管理员不能管理后台用户。
- 数据库用户不能被设置为 `SUPER_ADMIN`。
- 用户列表不返回密码摘要、盐、算法或初始密码。
- 停用用户不能登录，已有 token 也会失效。

验证命令：

```powershell
.\mvnw.cmd test
```

结果：`104 tests, 0 failures, 0 errors`。
