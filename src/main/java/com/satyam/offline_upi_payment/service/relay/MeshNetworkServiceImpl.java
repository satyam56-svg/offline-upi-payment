package com.satyam.offline_upi_payment.service.relay;

import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.service.bank.BankServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeshNetworkServiceImpl implements MeshNetworkService {

    private final RelayNodeA relayNodeA;

    private final RelayNodeB relayNodeB;

    private final RelayNodeC relayNodeC;

    private final BankServerService bankServerService;

    @Override
    public PaymentPacket transmit(PaymentPacket packet) {

        log.info("🚀 Starting Mesh Network Transmission");

        packet = relayNodeA.forward(packet);

        packet = relayNodeB.forward(packet);

        packet = relayNodeC.forward(packet);

        log.info("🏦 Packet reached Bank Server");

        return bankServerService.processPacket(packet);

    }
}