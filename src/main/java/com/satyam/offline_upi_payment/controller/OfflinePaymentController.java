package com.satyam.offline_upi_payment.controller;

import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.dto.PaymentRequest;
import com.satyam.offline_upi_payment.service.packet.PacketBuilderService;
import com.satyam.offline_upi_payment.service.relay.MeshNetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Offline Payment APIs",
        description = "Offline Mesh Network Payment Operations"
)
@RestController
@RequestMapping("/api/offline-payments")
@RequiredArgsConstructor
public class OfflinePaymentController {

    private final PacketBuilderService packetBuilderService;

    private final MeshNetworkService meshNetworkService;


    @Operation(
            summary = "Offline Payment",
            description = "Creates a secure payment packet and transmits it through the mesh network."
    )
    @PostMapping("/send")
    public PaymentPacket sendOfflinePayment(
            @RequestBody PaymentRequest request,
            Authentication authentication
    ) {

        request.setSenderUpiId(authentication.getName());

        PaymentPacket packet =
                packetBuilderService.buildPacket(request);

        return meshNetworkService.transmit(packet);

    }

}