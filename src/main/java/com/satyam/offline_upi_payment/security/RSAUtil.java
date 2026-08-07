package com.satyam.offline_upi_payment.security;

import org.springframework.stereotype.Component;

import java.security.*;
import javax.crypto.Cipher;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAUtil {
    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(
                Base64.getDecoder().decode(encryptedData)
        );
        return new String(decryptedBytes);
    }

    public String signData(String data, PrivateKey privateKey) throws Exception {

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initSign(privateKey);

        signature.update(data.getBytes());

        byte[] signedBytes = signature.sign();

        return Base64.getEncoder().encodeToString(signedBytes);

    }

    public boolean verifySignature(
            String data,
            String signatureText,
            PublicKey publicKey) throws Exception {

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initVerify(publicKey);

        signature.update(data.getBytes());

        byte[] signatureBytes =
                Base64.getDecoder().decode(signatureText);

        return signature.verify(signatureBytes);
    }

    public PrivateKey getPrivateKeyFromString(String privateKeyString)
            throws Exception {

        byte[] keyBytes =
                Base64.getDecoder().decode(privateKeyString);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey getPublicKeyFromString(String publicKeyString)
            throws Exception {

        byte[] keyBytes =
                Base64.getDecoder().decode(publicKeyString);

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }
}