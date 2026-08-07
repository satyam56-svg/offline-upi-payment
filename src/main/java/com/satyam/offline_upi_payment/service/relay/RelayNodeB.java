package com.satyam.offline_upi_payment.service.relay;

import com.satyam.offline_upi_payment.dto.PacketStatus;
import com.satyam.offline_upi_payment.dto.PaymentPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RelayNodeB {

    public PaymentPacket forward(PaymentPacket packet) {

        log.info(
                "📡 Relay Node B received packet : {}",
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
        packet.addEvent("RELAY_B");
        return packet;
    }
}