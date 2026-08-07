package com.satyam.offline_upi_payment.service.relay;

import com.satyam.offline_upi_payment.dto.PaymentPacket;

public interface MeshNetworkService {

    PaymentPacket transmit(PaymentPacket packet);

}