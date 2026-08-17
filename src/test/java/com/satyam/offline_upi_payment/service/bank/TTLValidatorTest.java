package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.exception.PacketExpiredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TTLValidatorTest {

    @Test
    void shouldAcceptPacketWithPositiveTTL() {

        TTLValidator validator = new TTLValidator();

        PaymentPacket packet = PaymentPacket.builder()
                .ttl(5)
                .build();

        assertDoesNotThrow(() ->
                validator.validate(packet)
        );
    }

    @Test
    void shouldRejectPacketWithZeroTTL() {

        TTLValidator validator = new TTLValidator();

        PaymentPacket packet = PaymentPacket.builder()
                .ttl(0)
                .build();

        assertThrows(
                PacketExpiredException.class,
                () -> validator.validate(packet)
        );
    }

    @Test
    void shouldRejectPacketWithNegativeTTL() {

        TTLValidator validator = new TTLValidator();

        PaymentPacket packet = PaymentPacket.builder()
                .ttl(-1)
                .build();

        assertThrows(
                PacketExpiredException.class,
                () -> validator.validate(packet)
        );
    }
}