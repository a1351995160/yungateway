# B-001 Validation

Valid. `UserAssertionService` validates HMAC, timestamp, nonce, and matching query/header `userId`, but it does not verify that the calling business system is authorized to act as that user. The WPS user token cache is intentionally `userId` only, so the boundary must be enforced before cache lookup or through an equivalent caller/user binding.
