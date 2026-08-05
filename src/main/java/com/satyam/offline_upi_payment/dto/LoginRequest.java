package com.satyam.offline_upi_payment.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String upiId;

    private String password;
}