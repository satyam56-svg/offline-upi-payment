package com.satyam.offline_upi_payment.service;

import com.satyam.offline_upi_payment.constant.PaymentStatus;
import com.satyam.offline_upi_payment.dto.PaymentRequest;
import com.satyam.offline_upi_payment.dto.PaymentResponse;
import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.exception.InsufficientBalanceException;
import com.satyam.offline_upi_payment.exception.InvalidAmountException;
import com.satyam.offline_upi_payment.exception.UserNotFoundException;
import com.satyam.offline_upi_payment.repository.PaymentRepository;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RSAUtil rsaUtil;

    private PaymentServiceImpl paymentService;

    private User sender;
    private User receiver;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                userRepository,
                walletRepository,
                rsaUtil
        );

        sender = new User();
        sender.setId(1L);
        sender.setUpiId("rahul@upi");
        sender.setPrivateKey("sender-private-key");
        sender.setPublicKey("sender-public-key");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUpiId("aman@upi");
        receiver.setPrivateKey("receiver-private-key");
        receiver.setPublicKey("receiver-public-key");

        senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setBalance(1400.0);
        senderWallet.setUser(sender);

        receiverWallet = new Wallet();
        receiverWallet.setId(2L);
        receiverWallet.setBalance(300.0);
        receiverWallet.setUser(receiver);
    }

    @Test
    void shouldSendMoneySuccessfully() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setSenderUpiId("rahul@upi");
        request.setReceiverUpiId("aman@upi");
        request.setAmount(500.0);

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUpiId("aman@upi"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUser(sender))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

        java.security.KeyPair keyPair =
                java.security.KeyPairGenerator
                        .getInstance("RSA")
                        .generateKeyPair();

        when(rsaUtil.getPrivateKeyFromString("sender-private-key"))
                .thenReturn(keyPair.getPrivate());

        when(rsaUtil.signData(
                any(String.class),
                any()
        )).thenReturn("test-signature");

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {

                    Payment payment = invocation.getArgument(0);
                    payment.setId(100L);
                    return payment;
                });

        when(rsaUtil.getPublicKeyFromString("sender-public-key"))
                .thenReturn(keyPair.getPublic());

        when(rsaUtil.verifySignature(
                any(String.class),
                eq("test-signature"),
                any()
        )).thenReturn(true);

        PaymentResponse response =
                paymentService.sendMoney(request);

        assertNotNull(response);

        assertEquals(100L, response.getPaymentId());
        assertEquals("rahul@upi", response.getSenderUpiId());
        assertEquals("aman@upi", response.getReceiverUpiId());
        assertEquals(500.0, response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertTrue(response.isSignatureValid());

        assertEquals(900.0, senderWallet.getBalance());
        assertEquals(800.0, receiverWallet.getBalance());

        verify(walletRepository).save(senderWallet);
        verify(walletRepository).save(receiverWallet);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldRejectInsufficientBalance() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setSenderUpiId("rahul@upi");
        request.setReceiverUpiId("aman@upi");
        request.setAmount(1500.0);

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUpiId("aman@upi"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUser(sender))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> paymentService.sendMoney(request)
        );

        assertEquals(1400.0, senderWallet.getBalance());
        assertEquals(300.0, receiverWallet.getBalance());

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldRejectSelfPayment() {

        PaymentRequest request = new PaymentRequest();
        request.setSenderUpiId("rahul@upi");
        request.setReceiverUpiId("rahul@upi");
        request.setAmount(500.0);

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        assertThrows(
                RuntimeException.class,
                () -> paymentService.sendMoney(request)
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldRejectInvalidAmount() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setSenderUpiId("rahul@upi");
        request.setReceiverUpiId("aman@upi");
        request.setAmount(0.0);

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUpiId("aman@upi"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUser(sender))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

        assertThrows(
                InvalidAmountException.class,
                () -> paymentService.sendMoney(request)
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldRejectUnknownReceiver() {

        PaymentRequest request = new PaymentRequest();
        request.setSenderUpiId("rahul@upi");
        request.setReceiverUpiId("unknown@upi");
        request.setAmount(500.0);

        when(userRepository.findByUpiId("rahul@upi"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUpiId("unknown@upi"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> paymentService.sendMoney(request)
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}