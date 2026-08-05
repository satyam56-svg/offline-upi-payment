package com.satyam.offline_upi_payment.dto;

import lombok.Data;

@Data
public class EncryptedPaymentRequest {

    private String encryptedData;

    private String encryptedKey;

}