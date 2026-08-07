package com.satyam.offline_upi_payment.service;
import com.satyam.offline_upi_payment.dto.PaymentResponse;

import com.satyam.offline_upi_payment.dto.PaymentRequest;
import com.satyam.offline_upi_payment.entity.Payment;

import java.util.List;

public interface PaymentService {

    PaymentResponse sendMoney(PaymentRequest request);
    List<Payment> getTransactionHistory(String upiId);
}