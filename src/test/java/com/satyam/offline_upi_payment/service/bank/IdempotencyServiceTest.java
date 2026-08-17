package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.exception.DuplicatePacketException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyServiceTest {

    @Test
    void shouldAcceptNewPacket() {

        IdempotencyService service = new IdempotencyService();

        assertDoesNotThrow(() ->
                service.validate("packet-123")
        );
    }

    @Test
    void shouldRejectDuplicatePacket() {

        IdempotencyService service = new IdempotencyService();

        service.validate("packet-123");

        assertThrows(
                DuplicatePacketException.class,
                () -> service.validate("packet-123")
        );
    }

    @Test
    void shouldAcceptDifferentPackets() {

        IdempotencyService service = new IdempotencyService();

        service.validate("packet-123");

        assertDoesNotThrow(() ->
                service.validate("packet-456")
        );
    }
}