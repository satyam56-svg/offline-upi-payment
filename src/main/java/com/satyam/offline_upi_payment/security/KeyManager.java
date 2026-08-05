package com.satyam.offline_upi_payment.security;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

@Component
@Getter
public class KeyManager {

    private final KeyPair keyPair;

    public KeyManager(RSAUtil rsaUtil) throws Exception {

        this.keyPair = rsaUtil.generateKeyPair();

    }

}