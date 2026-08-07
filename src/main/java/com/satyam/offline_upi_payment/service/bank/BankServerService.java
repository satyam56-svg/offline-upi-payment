package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.dto.PaymentPacket;

public interface BankServerService {

    PaymentPacket processPacket(PaymentPacket packet);

}