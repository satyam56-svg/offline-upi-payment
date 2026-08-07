package com.satyam.offline_upi_payment.service.packet;

import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.dto.PaymentRequest;

public interface PacketBuilderService {

    PaymentPacket buildPacket(PaymentRequest request);

}