package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.exception.PacketExpiredException;
import org.springframework.stereotype.Component;

@Component
public class TTLValidator {

    public void validate(PaymentPacket packet) {

        if (packet.getTtl() <= 0) {

            throw new PacketExpiredException(
                    "Packet expired (TTL reached zero)"
            );

        }

    }

}