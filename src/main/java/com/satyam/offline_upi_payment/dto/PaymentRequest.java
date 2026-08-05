package com.satyam.offline_upi_payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private String senderUpiId;

    private String receiverUpiId;

    private Double amount;
}