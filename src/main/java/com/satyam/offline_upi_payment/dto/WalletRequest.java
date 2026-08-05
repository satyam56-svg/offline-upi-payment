package com.satyam.offline_upi_payment.dto;

import lombok.Data;

@Data
public class WalletRequest {

    private String upiId;

    private double amount;
}