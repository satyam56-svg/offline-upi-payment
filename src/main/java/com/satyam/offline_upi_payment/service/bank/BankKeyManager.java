package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.security.RSAUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@RequiredArgsConstructor
@Getter
public class BankKeyManager {

    private final RSAUtil rsaUtil;

    private PublicKey bankPublicKey;

    private PrivateKey bankPrivateKey;

    @PostConstruct
    public void initialize() {

        try {

            KeyPair keyPair = rsaUtil.generateKeyPair();

            bankPublicKey = keyPair.getPublic();

            bankPrivateKey = keyPair.getPrivate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to initialize Bank RSA Key Pair",
                    e
            );

        }

    }

}