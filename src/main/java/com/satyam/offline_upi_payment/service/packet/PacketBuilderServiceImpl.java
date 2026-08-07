package com.satyam.offline_upi_payment.service.packet;

import com.satyam.offline_upi_payment.dto.*;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.CryptoService;
import com.satyam.offline_upi_payment.service.bank.BankKeyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacketBuilderServiceImpl
        implements PacketBuilderService {

    private final CryptoService cryptoService;
    private final BankKeyManager bankKeyManager;
    private final RSAUtil rsaUtil;
    private final UserRepository userRepository;

    @Override
    public PaymentPacket buildPacket(
            PaymentRequest request
    ) {

        EncryptedPayload payload = EncryptedPayload.builder()
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .nonce(UUID.randomUUID().toString())
                .createdAt(System.currentTimeMillis())
                .build();

        EncryptedData encryptedData;
        String encryptedAESKey;
        String digitalSignature;

        try {

            encryptedData = cryptoService.encryptPayload(payload);

            encryptedAESKey = rsaUtil.encrypt(
                    encryptedData.getAesKey(),
                    bankKeyManager.getBankPublicKey()
            );

            String senderPrivateKey = userRepository
                    .findByUpiId(request.getSenderUpiId())
                    .orElseThrow(() ->
                            new RuntimeException("Sender not found"))
                    .getPrivateKey();

            digitalSignature = cryptoService.sign(
                    encryptedData.getEncryptedPayload(),
                    senderPrivateKey
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to build secure payment packet",
                    e
            );

        }
        PaymentPacket packet = PaymentPacket.builder()

                .packetId(UUID.randomUUID().toString())

                .senderUpiId(request.getSenderUpiId())

                .encryptedPayload(
                        encryptedData.getEncryptedPayload()
                )

                .encryptedAESKey(
                        encryptedAESKey
                )

                .digitalSignature(
                        digitalSignature
                )

                .iv(
                        encryptedData.getIv()
                )

                .ttl(5)

                .hopCount(0)

                .timestamp(System.currentTimeMillis())

                .status(PacketStatus.CREATED)

                .build();

        packet.addEvent("PACKET_CREATED");

        return packet;
    }

}