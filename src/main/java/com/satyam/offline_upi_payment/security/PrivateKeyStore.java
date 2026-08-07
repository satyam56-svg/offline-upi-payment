package com.satyam.offline_upi_payment.security;

import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

@Component
public class PrivateKeyStore {

    private final Map<String, PrivateKey> privateKeys = new HashMap<>();

    public void savePrivateKey(String upiId, PrivateKey privateKey) {
        privateKeys.put(upiId, privateKey);
    }

    public PrivateKey getPrivateKey(String upiId) {
        return privateKeys.get(upiId);
    }
}
