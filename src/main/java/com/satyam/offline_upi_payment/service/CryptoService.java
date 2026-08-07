package com.satyam.offline_upi_payment.service;

import com.satyam.offline_upi_payment.dto.EncryptedData;
import com.satyam.offline_upi_payment.dto.EncryptedPayload;

public interface CryptoService {

    EncryptedData encryptPayload(EncryptedPayload payload) throws Exception;

    EncryptedPayload decryptPayload(
            String encryptedPayload,
            String aesKey,
            String iv
    ) throws Exception;

    String sign(
            String data,
            String privateKey
    ) throws Exception;

    boolean verify(
            String data,
            String signature,
            String publicKey
    ) throws Exception;

}