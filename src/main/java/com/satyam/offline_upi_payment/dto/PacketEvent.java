package com.satyam.offline_upi_payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketEvent {

    private String stage;

    private long timestamp;

}