package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.constant.PaymentStatus;
import com.satyam.offline_upi_payment.dto.EncryptedPayload;
import com.satyam.offline_upi_payment.dto.PacketStatus;
import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.exception.*;
import com.satyam.offline_upi_payment.repository.PaymentRepository;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.CryptoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Service
public class BankServerServiceImpl implements BankServerService {

    private final TTLValidator ttlValidator;
    private final IdempotencyService idempotencyService;
    private final RSAUtil rsaUtil;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final BankKeyManager bankKeyManager;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    @Override
    public PaymentPacket processPacket(PaymentPacket packet) {

        log.info("🏦 Bank Server received packet : {}",
                packet.getPacketId());
        packet.addEvent("BANK_RECEIVED");

        ttlValidator.validate(packet);
        log.info("✅ TTL validation passed");
        idempotencyService.validate(
                packet.getPacketId()
        );
        log.info("✅ Duplicate packet check passed");

        String senderPublicKey = userRepository
                .findByUpiId(packet.getSenderUpiId())
                .orElseThrow(() ->
                        new UserNotFoundException("Sender not found"))
                .getPublicKey();

        boolean isValid;

        try {

            isValid = cryptoService.verify(
                    packet.getEncryptedPayload(),
                    packet.getDigitalSignature(),
                    senderPublicKey
            );
            log.info("🔐 Digital signature verified");
            packet.addEvent("SIGNATURE_VERIFIED");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to verify digital signature",
                    e
            );

        }

        if (!isValid) {

            throw new InvalidSignatureException(
                    "Invalid digital signature"
            );

        }

        String aesKey;

        try {

            aesKey = rsaUtil.decrypt(
                    packet.getEncryptedAESKey(),
                    bankKeyManager.getBankPrivateKey()
            );
            log.info("🔓 AES key decrypted");
            packet.addEvent("AES_KEY_DECRYPTED");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to decrypt AES key",
                    e
            );

        }

        EncryptedPayload payload;

        try {

            payload = cryptoService.decryptPayload(
                    packet.getEncryptedPayload(),
                    aesKey,
                    packet.getIv()
            );
            log.info("📦 Payment payload decrypted");
            packet.addEvent("PAYLOAD_DECRYPTED");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to decrypt payment payload",
                    e
            );

        }

        if (!packet.getSenderUpiId().equals(payload.getSenderUpiId())) {

            throw new SenderMismatchException(
                    "Sender mismatch detected"
            );

        }

        User sender = userRepository
                .findByUpiId(payload.getSenderUpiId())
                .orElseThrow(() ->
                        new UserNotFoundException("Sender not found"));
        log.info("👤 Sender verified : {}", sender.getUpiId());

        User receiver = userRepository
                .findByUpiId(payload.getReceiverUpiId())
                .orElseThrow(() ->
                        new UserNotFoundException("Receiver not found"));
        log.info("👤 Receiver verified : {}", receiver.getUpiId());

        if (sender.getId().equals(receiver.getId())) {

            throw new RuntimeException(
                    "Cannot send money to yourself"
            );

        }

        Wallet senderWallet = walletRepository
                .findByUser(sender)
                .orElseThrow(() ->
                        new RuntimeException("Sender wallet not found"));
        log.info(
                "💸 Debiting ₹{} from {}",
                payload.getAmount(),
                sender.getUpiId()
        );

        Wallet receiverWallet = walletRepository
                .findByUser(receiver)
                .orElseThrow(() ->
                        new RuntimeException("Receiver wallet not found"));

        if (payload.getAmount() <= 0) {

            throw new InvalidAmountException(
                    "Amount must be greater than zero"
            );

        }

        if (senderWallet.getBalance() < payload.getAmount()) {

            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );

        }

        senderWallet.setBalance(
                senderWallet.getBalance() - payload.getAmount()
        );

        receiverWallet.setBalance(
                receiverWallet.getBalance() + payload.getAmount()
        );

        log.info(
                "💰 Crediting ₹{} to {}",
                payload.getAmount(),
                receiver.getUpiId()
        );

        walletRepository.save(senderWallet);

        walletRepository.save(receiverWallet);

        Payment payment = Payment.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(payload.getAmount())
                .status(PaymentStatus.SUCCESS)
                .signature(packet.getDigitalSignature())
                .timestamp(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("📝 Payment transaction saved successfully");
        packet.setStatus(PacketStatus.PROCESSED);
        log.info(
                "✅ Packet {} processed successfully",
                packet.getPacketId()
        );
        packet.addEvent("PAYMENT_PROCESSED");

        return packet;
    }

}