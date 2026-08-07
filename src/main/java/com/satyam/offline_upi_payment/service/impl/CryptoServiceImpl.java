package com.satyam.offline_upi_payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satyam.offline_upi_payment.dto.AESResult;
import com.satyam.offline_upi_payment.dto.EncryptedData;
import com.satyam.offline_upi_payment.dto.EncryptedPayload;
import com.satyam.offline_upi_payment.security.AESUtil;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CryptoServiceImpl implements CryptoService {

    private final AESUtil aesUtil;

    private final RSAUtil rsaUtil;

    private final ObjectMapper objectMapper;

    @Override
    public EncryptedData encryptPayload(EncryptedPayload payload) throws Exception {

        String json = objectMapper.writeValueAsString(payload);

        SecretKey aesKey = aesUtil.generateKey();

        AESResult aesResult = aesUtil.encrypt(json, aesKey);

        return EncryptedData.builder()
                .encryptedPayload(aesResult.getEncryptedData())
                .aesKey(
                        Base64.getEncoder()
                                .encodeToString(aesKey.getEncoded())
                )
                .iv(aesResult.getIv())
                .build();
    }

    @Override
    public EncryptedPayload decryptPayload(
            String encryptedPayload,
            String aesKey,
            String iv
    ) throws Exception {

        SecretKey secretKey = aesUtil.getSecretKeyFromString(aesKey);

        String json = aesUtil.decrypt(
                encryptedPayload,
                secretKey,
                iv
        );

        return objectMapper.readValue(
                json,
                EncryptedPayload.class
        );
    }

    @Override
    public String sign(
            String data,
            String privateKey
    ) throws Exception {

        return rsaUtil.signData(
                data,
                rsaUtil.getPrivateKeyFromString(privateKey)
        );
    }

    @Override
    public boolean verify(
            String data,
            String signature,
            String publicKey
    ) throws Exception {

        return rsaUtil.verifySignature(
                data,
                signature,
                rsaUtil.getPublicKeyFromString(publicKey)
        );
    }

}