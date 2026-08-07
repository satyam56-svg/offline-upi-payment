package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.exception.DuplicatePacketException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class IdempotencyService {

    private final Set<String> processedPackets =
            new HashSet<>();

    public void validate(String packetId) {

        if (processedPackets.contains(packetId)) {

            throw new DuplicatePacketException(
                    "Duplicate payment packet detected."
            );

        }

        processedPackets.add(packetId);

    }

}