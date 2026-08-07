package com.satyam.offline_upi_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.crypto.SecretKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptedData {

    private String encryptedPayload;
    private String aesKey;
    private String iv;

}