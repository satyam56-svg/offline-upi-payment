package com.satyam.offline_upi_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;

    private String senderUpiId;

    private String receiverUpiId;

    private Double amount;

    private String status;

    private boolean signatureValid;
}