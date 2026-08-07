package com.satyam.offline_upi_payment.security;

import com.satyam.offline_upi_payment.dto.AESResult;
import org.springframework.stereotype.Component;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Component
public class AESUtil {

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public AESResult encrypt(String data, SecretKey secretKey) throws Exception {

        byte[] iv = new byte[12];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(128, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                gcmParameterSpec
        );

        byte[] encryptedBytes =
                cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return AESResult.builder()
                .encryptedData(
                        Base64.getEncoder().encodeToString(encryptedBytes)
                )
                .iv(
                        Base64.getEncoder().encodeToString(iv)
                )
                .build();
    }

    public String decrypt(
            String encryptedData,
            SecretKey secretKey,
            String iv
    ) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(
                        128,
                        Base64.getDecoder().decode(iv)
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                gcmParameterSpec
        );

        byte[] decryptedBytes = cipher.doFinal(
                Base64.getDecoder().decode(encryptedData)
        );

        return new String(
                decryptedBytes,
                StandardCharsets.UTF_8
        );
    }

    public SecretKey getSecretKeyFromString(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, "AES");

    }

}