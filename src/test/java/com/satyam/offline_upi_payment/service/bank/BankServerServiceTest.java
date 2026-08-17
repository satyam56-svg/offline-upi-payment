package com.satyam.offline_upi_payment.service.bank;

import com.satyam.offline_upi_payment.constant.PaymentStatus;
import com.satyam.offline_upi_payment.dto.EncryptedPayload;
import com.satyam.offline_upi_payment.dto.PacketStatus;
import com.satyam.offline_upi_payment.dto.PaymentPacket;
import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.repository.PaymentRepository;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PrivateKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServerServiceTest {

    @Mock
    private TTLValidator ttlValidator;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private RSAUtil rsaUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private BankKeyManager bankKeyManager;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private BankServerServiceImpl bankServerService;

    private User sender;
    private User receiver;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    private PaymentPacket packet;
    private EncryptedPayload payload;

    @BeforeEach
    void setUp() {

        bankServerService = new BankServerServiceImpl(
                ttlValidator,
                idempotencyService,
                rsaUtil,
                userRepository,
                cryptoService,
                bankKeyManager,
                walletRepository,
                paymentRepository
        );

        sender = new User();
        sender.setId(1L);
        sender.setUpiId("rahul@upi");
        sender.setPublicKey("sender-public-key");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUpiId("aman@upi");

        senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(sender);
        senderWallet.setBalance(1400.0);

        receiverWallet = new Wallet();
        receiverWallet.setId(2L);
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(300.0);

        packet = PaymentPacket.builder()
                .packetId("packet-123")
                .senderUpiId("rahul@upi")
                .encryptedPayload("encrypted-payload")
                .encryptedAESKey("encrypted-aes-key")
                .digitalSignature("digital-signature")
                .iv("test-iv")
                .ttl(5)
                .hopCount(3)
                .timestamp(System.currentTimeMillis())
                .status(PacketStatus.CREATED)
                .build();

        payload = EncryptedPayload.builder()
                .senderUpiId("rahul@upi")
                .receiverUpiId("aman@upi")
                .amount(500.0)
                .nonce("test-nonce")
                .createdAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void shouldProcessPaymentPacketSuccessfully() throws Exception {

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUpiId("aman@upi"))
                .thenReturn(Optional.of(receiver));

        when(cryptoService.verify(
                "encrypted-payload",
                "digital-signature",
                "sender-public-key"
        )).thenReturn(true);

        when(bankKeyManager.getBankPrivateKey())
                .thenReturn(mock(PrivateKey.class));

        when(rsaUtil.decrypt(
                eq("encrypted-aes-key"),
                any(PrivateKey.class)
        )).thenReturn("decrypted-aes-key");

        when(cryptoService.decryptPayload(
                "encrypted-payload",
                "decrypted-aes-key",
                "test-iv"
        )).thenReturn(payload);

        when(walletRepository.findByUser(sender))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentPacket result =
                bankServerService.processPacket(packet);

        assertNotNull(result);

        assertEquals(
                PacketStatus.PROCESSED,
                result.getStatus()
        );

        assertEquals(
                900.0,
                senderWallet.getBalance()
        );

        assertEquals(
                800.0,
                receiverWallet.getBalance()
        );

        verify(ttlValidator)
                .validate(packet);

        verify(idempotencyService)
                .validate("packet-123");

        verify(cryptoService)
                .verify(
                        "encrypted-payload",
                        "digital-signature",
                        "sender-public-key"
                );

        verify(rsaUtil)
                .decrypt(
                        eq("encrypted-aes-key"),
                        any(PrivateKey.class)
                );

        verify(cryptoService)
                .decryptPayload(
                        "encrypted-payload",
                        "decrypted-aes-key",
                        "test-iv"
                );

        verify(walletRepository)
                .save(senderWallet);

        verify(walletRepository)
                .save(receiverWallet);

        verify(paymentRepository)
                .save(any(Payment.class));

        assertTrue(
                result.getTimeline()
                        .stream()
                        .anyMatch(event ->
                                event.getStage()
                                        .equals("PAYMENT_PROCESSED")
                        )
        );
    }
    @Test
    void shouldRejectInvalidDigitalSignature() throws Exception {

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(cryptoService.verify(
                "encrypted-payload",
                "digital-signature",
                "sender-public-key"
        )).thenReturn(false);

        assertThrows(
                com.satyam.offline_upi_payment.exception.InvalidSignatureException.class,
                () -> bankServerService.processPacket(packet)
        );

        verify(rsaUtil, never())
                .decrypt(anyString(), any(PrivateKey.class));

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
    @Test
    void shouldRejectSenderMismatch() throws Exception {

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(cryptoService.verify(
                "encrypted-payload",
                "digital-signature",
                "sender-public-key"
        )).thenReturn(true);

        when(bankKeyManager.getBankPrivateKey())
                .thenReturn(mock(PrivateKey.class));

        when(rsaUtil.decrypt(
                eq("encrypted-aes-key"),
                any(PrivateKey.class)
        )).thenReturn("decrypted-aes-key");

        EncryptedPayload mismatchedPayload =
                EncryptedPayload.builder()
                        .senderUpiId("someoneelse@upi")
                        .receiverUpiId("aman@upi")
                        .amount(500.0)
                        .nonce("test-nonce")
                        .createdAt(System.currentTimeMillis())
                        .build();

        when(cryptoService.decryptPayload(
                "encrypted-payload",
                "decrypted-aes-key",
                "test-iv"
        )).thenReturn(mismatchedPayload);

        assertThrows(
                com.satyam.offline_upi_payment.exception.SenderMismatchException.class,
                () -> bankServerService.processPacket(packet)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
    @Test
    void shouldRejectInsufficientBalance() throws Exception {

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(cryptoService.verify(
                "encrypted-payload",
                "digital-signature",
                "sender-public-key"
        )).thenReturn(true);

        when(bankKeyManager.getBankPrivateKey())
                .thenReturn(mock(PrivateKey.class));

        when(rsaUtil.decrypt(
                eq("encrypted-aes-key"),
                any(PrivateKey.class)
        )).thenReturn("decrypted-aes-key");

        when(cryptoService.decryptPayload(
                "encrypted-payload",
                "decrypted-aes-key",
                "test-iv"
        )).thenReturn(payload);

        when(userRepository.findByUpiId("aman@upi"))
                .thenReturn(Optional.of(receiver));

        senderWallet.setBalance(100.0);

        when(walletRepository.findByUser(sender))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

        assertThrows(
                com.satyam.offline_upi_payment.exception.InsufficientBalanceException.class,
                () -> bankServerService.processPacket(packet)
        );

        assertEquals(100.0, senderWallet.getBalance());
        assertEquals(300.0, receiverWallet.getBalance());

        verify(walletRepository, never())
                .save(any(Wallet.class));

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
    @Test
    void shouldRejectDuplicatePacket() throws Exception {

        doThrow(
                new com.satyam.offline_upi_payment.exception.DuplicatePacketException(
                        "Duplicate payment packet detected."
                )
        ).when(idempotencyService)
                .validate("packet-123");

        assertThrows(
                com.satyam.offline_upi_payment.exception.DuplicatePacketException.class,
                () -> bankServerService.processPacket(packet)
        );

        verify(userRepository, never())
                .findByUpiId(anyString());

        verify(cryptoService, never())
                .verify(anyString(), anyString(), anyString());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
    @Test
    void shouldRejectExpiredPacket() {

        doThrow(
                new com.satyam.offline_upi_payment.exception.PacketExpiredException(
                        "Packet expired (TTL reached zero)"
                )
        ).when(ttlValidator)
                .validate(packet);

        assertThrows(
                com.satyam.offline_upi_payment.exception.PacketExpiredException.class,
                () -> bankServerService.processPacket(packet)
        );

        verify(idempotencyService, never())
                .validate(anyString());

        verify(userRepository, never())
                .findByUpiId(anyString());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
}