# WPS 云文档能力接入与请求签名设计

## 设计结论

本文档记录当前能力网关的安全边界和后续实现方向，适用于 APP 预览和 USER 文件能力。

核心结论：

- 业务系统负责确认平台用户身份、用户是否有权操作某个文件、以及平台用户和 WPS 账号的真实绑定关系。
- 能力网关负责确认请求来自已接入业务系统、请求未被篡改、请求未被重放、业务系统拥有对应能力权限，并安全地调用 WPS。
- WPS USER token 按 `userId` 全业务系统复用，这是当前产品决策。网关不按业务系统拆分 WPS USER token 缓存。
- `clientSecret` 只用于换取内部 JWT；能力请求签名使用独立的请求签名密钥。
- 请求签名密钥按当前决策全业务系统共用，不为每个业务系统单独创建一把签名 key。

## 能力调用边界

业务系统调用能力接口前，先用 `clientId + clientSecret` 换取内部 JWT：

```http
POST /api/v1/auth/token
```

后续能力接口必须带：

```http
Authorization: Bearer <internal-jwt>
```

网关会校验：

- JWT 签名、过期时间、issuer、audience 和 token 类型。
- JWT 中的 `businessSystemId`、`clientId`、`tokenVersion`、`permissionVersion`。
- 业务系统状态必须启用。
- 当前接口对应的能力权限必须启用，例如 `app-preview:create` 或 `user-files:list`。

## 请求签名职责

请求签名用于证明请求内容没有被篡改，并防止短时间内重放。它不证明业务系统是否拥有某个最终用户或某个文件。

通用能力请求签名头目标采用：

```http
X-Yundoc-Signature-Key-Id: v1
X-Yundoc-Timestamp: 1780110000
X-Yundoc-Nonce: random-nonce
X-Yundoc-Signature: <base64url-hmac-sha256>
```

签名算法：

```text
base64url(HMAC-SHA256(canonicalText, requestSigningSecret))
```

timestamp 必须在服务端允许时间窗内，nonce 在时间窗内只能使用一次。

## USER 文件能力

USER 文件能力当前已经使用 USER 断言签名。USER 断言为了明确绑定操作用户，使用 `X-Yundoc-User-*` 头；后续 APP 预览签名可以使用通用能力请求签名头。请求示例：

```http
GET /api/v1/user/files?userId=user-001&parentFileId=root&limit=50
Authorization: Bearer <internal-jwt>
X-Yundoc-User-Id: user-001
X-Yundoc-User-Timestamp: 1780110000
X-Yundoc-User-Nonce: random-nonce
X-Yundoc-User-Key-Id: v1
X-Yundoc-User-Signature: <signature>
```

当前 USER 断言 canonical text：

```text
method
path
queryString
businessSystemId
clientId
userId
timestamp
nonce
```

网关校验：

- query 中的 `userId` 和 `X-Yundoc-User-Id` 必须一致。
- `businessSystemId + clientId` 来自内部 JWT，不由调用方传入。
- timestamp 在允许时间窗内。
- nonce 在 `businessSystemId + clientId` 维度下不可重放。
- HMAC 签名匹配。

USER token 缓存策略：

```text
userId -> WPS_USER_TOKEN
```

该缓存不包含 `businessSystemId`。原因是 WPS 授权是平台用户到 WPS 的授权，不希望不同业务系统重复触发同一用户的 WPS OAuth 授权。

## APP 预览能力

APP 预览后续也要增加请求签名。目标是防止 `fileId`、`expireSeconds`、请求路径和请求方法被篡改或重放。

推荐 APP 预览 canonical text：

```text
method
path
queryString
businessSystemId
clientId
source.type
source.fileId
options.expireSeconds
timestamp
nonce
```

APP 预览签名不负责判断 `fileId` 是否属于某个最终用户或某个业务系统。这个归属关系由业务系统负责，网关只验证请求完整性、业务权限和 WPS 返回内容安全。

## WPS OAuth 回调责任边界

当前责任边界：

- 业务系统负责保证传入的 `userId` 是真实平台用户。
- 业务系统负责保证该用户和实际完成授权的 WPS 账号之间的关系有效。
- 网关通过 OAuth `state` 把本次授权结果写入对应 `userId` 的 WPS token 缓存。

因此，网关暂不强制校验 WPS 授权账号和平台 `userId` 的归属关系。若后续产品要求网关承担账号绑定责任，需要在 OAuth callback 后增加 WPS 用户信息查询，并和平台用户映射表比对。

## 预览链接安全

WPS 预览接口返回的是第三方直链时，网关无法在链接发出后强制缩短它的有效期。因此当前目标分两层：

第一层，必须校验 WPS 返回值：

- `previewUrl` 必须是 `https`。
- host 必须在允许列表中。
- URL 不能包含 userinfo。
- `expireAt` 必须可解析。
- `expireAt` 不能明显超过本次请求的 `expireSeconds`，只允许少量时钟偏差。

第二层，如需网关强制有效期：

- 不直接返回 WPS 预览 URL。
- 返回网关生成的短链或跳转链接。
- 网关保存短链、WPS URL、过期时间和请求上下文。
- 访问短链时由网关判断是否过期，未过期才跳转或代理到 WPS。

## 当前实现状态

已实现：

- 内部 JWT 换取与能力权限校验。
- USER 文件列表请求签名、timestamp 校验和 nonce 防重放。
- WPS USER token 按 `userId` 全业务系统复用。

待实现：

- APP 预览请求签名。
- WPS 预览 URL 的 `https`、host、userinfo 和 `expireAt` 校验。
- 如产品要求强制网关有效期，再实现网关短链或代理预览链接。

明确不做：

- 不要求每个业务系统单独一把请求签名 key。
- 不在能力网关内判断业务系统是否能代表某个 `userId`。
- 不在能力网关内判断某个 `fileId` 是否归属于某个用户或业务系统，除非后续新增网关侧授权表或文件授权凭证。
