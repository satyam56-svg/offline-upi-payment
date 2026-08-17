package com.satyam.offline_upi_payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satyam.offline_upi_payment.dto.EncryptedData;
import com.satyam.offline_upi_payment.dto.EncryptedPayload;
import com.satyam.offline_upi_payment.security.AESUtil;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.impl.CryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private CryptoService cryptoService;
    private RSAUtil rsaUtil;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {

        AESUtil aesUtil = new AESUtil();
        rsaUtil = new RSAUtil();
        ObjectMapper objectMapper = new ObjectMapper();

        cryptoService = new CryptoServiceImpl(
                aesUtil,
                rsaUtil,
                objectMapper
        );

        keyPair = rsaUtil.generateKeyPair();
    }

    @Test
    void shouldEncryptAndDecryptPayload() throws Exception {

        EncryptedPayload originalPayload =
                EncryptedPayload.builder()
                        .senderUpiId("rahul@upi")
                        .receiverUpiId("aman@upi")
                        .amount(500.0)
                        .nonce("test-nonce")
                        .createdAt(System.currentTimeMillis())
                        .build();

        EncryptedData encryptedData =
                cryptoService.encryptPayload(originalPayload);

        assertNotNull(encryptedData);
        assertNotNull(encryptedData.getEncryptedPayload());
        assertNotNull(encryptedData.getAesKey());
        assertNotNull(encryptedData.getIv());

        EncryptedPayload decryptedPayload =
                cryptoService.decryptPayload(
                        encryptedData.getEncryptedPayload(),
                        encryptedData.getAesKey(),
                        encryptedData.getIv()
                );

        assertEquals(
                originalPayload.getSenderUpiId(),
                decryptedPayload.getSenderUpiId()
        );

        assertEquals(
                originalPayload.getReceiverUpiId(),
                decryptedPayload.getReceiverUpiId()
        );

        assertEquals(
                originalPayload.getAmount(),
                decryptedPayload.getAmount()
        );

        assertEquals(
                originalPayload.getNonce(),
                decryptedPayload.getNonce()
        );
    }

    @Test
    void shouldGenerateAndVerifyDigitalSignature() throws Exception {

        String data = "rahul@upi|aman@upi|500.0";

        String privateKey =
                java.util.Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPrivate().getEncoded()
                        );

        String publicKey =
                java.util.Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPublic().getEncoded()
                        );

        String signature =
                cryptoService.sign(
                        data,
                        privateKey
                );

        assertNotNull(signature);
        assertFalse(signature.isBlank());

        boolean valid =
                cryptoService.verify(
                        data,
                        signature,
                        publicKey
                );

        assertTrue(valid);
    }

    @Test
    void shouldRejectTamperedData() throws Exception {

        String originalData =
                "rahul@upi|aman@upi|500.0";

        String tamperedData =
                "rahul@upi|aman@upi|1000.0";

        String privateKey =
                java.util.Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPrivate().getEncoded()
                        );

        String publicKey =
                java.util.Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPublic().getEncoded()
                        );

        String signature =
                cryptoService.sign(
                        originalData,
                        privateKey
                );

        boolean valid =
                cryptoService.verify(
                        tamperedData,
                        signature,
                        publicKey
                );

        assertFalse(valid);
    }
}