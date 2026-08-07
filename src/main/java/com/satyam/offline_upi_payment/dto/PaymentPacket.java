package com.satyam.offline_upi_payment.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPacket {

    private String packetId;

    private String senderUpiId;

    private String encryptedPayload;

    private String encryptedAESKey;

    private String digitalSignature;

    private Integer ttl;

    private Integer hopCount;

    private Long timestamp;

    private PacketStatus status;

    private String iv;

    @Builder.Default
    private List<PacketEvent> timeline = new ArrayList<>();

    public void addEvent(String stage) {

        timeline.add(
                PacketEvent.builder()
                        .stage(stage)
                        .timestamp(System.currentTimeMillis())
                        .build()
        );

    }

}