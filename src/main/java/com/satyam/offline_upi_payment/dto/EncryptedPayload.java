package com.satyam.offline_upi_payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptedPayload {

    private String senderUpiId;

    private String receiverUpiId;

    private Double amount;

    private String nonce;

    private Long createdAt;

}