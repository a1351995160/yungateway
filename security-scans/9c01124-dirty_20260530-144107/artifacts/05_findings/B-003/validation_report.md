# B-003 Validation

Valid. The gateway requests an expiry but only relays WPS response fields. It cannot enforce a shorter lifetime after a direct WPS URL is returned. It also does not validate scheme, host, userinfo, or response expiry against requested `expireSeconds`.
