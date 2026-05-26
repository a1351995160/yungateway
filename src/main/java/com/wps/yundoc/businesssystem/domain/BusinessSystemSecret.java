package com.wps.yundoc.businesssystem.domain;

public class BusinessSystemSecret {

    private final String clientSecretDigest;
    private final String clientSecretSalt;
    private final String clientSecretAlg;

    public BusinessSystemSecret(
            String clientSecretDigest,
            String clientSecretSalt,
            String clientSecretAlg) {
        this.clientSecretDigest = clientSecretDigest;
        this.clientSecretSalt = clientSecretSalt;
        this.clientSecretAlg = clientSecretAlg;
    }

    public String getClientSecretDigest() {
        return clientSecretDigest;
    }

    public String getClientSecretSalt() {
        return clientSecretSalt;
    }

    public String getClientSecretAlg() {
        return clientSecretAlg;
    }
}
