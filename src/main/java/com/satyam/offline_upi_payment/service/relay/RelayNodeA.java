package com.satyam.offline_upi_payment.service.relay;

import com.satyam.offline_upi_payment.dto.PacketStatus;
import com.satyam.offline_upi_payment.dto.PaymentPacket;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RelayNodeA {

    public PaymentPacket forward(PaymentPacket packet) {

        log.info(
                "📡 Relay Node A received packet : {}",
                packet.getPacketId()
        );

        packet.setHopCount(packet.getHopCount() + 1);

        packet.setTtl(packet.getTtl() - 1);
        System.out.println(
                "TTL = " + packet.getTtl()
                        + " | Hop = "
                        + packet.getHopCount()
        );
        packet.setStatus(PacketStatus.FORWARDED);
        packet.addEvent("RELAY_A");
        return packet;
    }
}