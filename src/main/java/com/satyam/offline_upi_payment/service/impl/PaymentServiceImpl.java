package com.satyam.offline_upi_payment.service.impl;

import com.satyam.offline_upi_payment.dto.PaymentRequest;
import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.repository.PaymentRepository;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import org.springframework.transaction.annotation.Transactional;
import com.satyam.offline_upi_payment.exception.UserNotFoundException;
import com.satyam.offline_upi_payment.exception.InvalidAmountException;
import com.satyam.offline_upi_payment.exception.InsufficientBalanceException;
import com.satyam.offline_upi_payment.constant.PaymentStatus;
import com.satyam.offline_upi_payment.security.RSAUtil;
import com.satyam.offline_upi_payment.dto.PaymentResponse;
import java.security.PublicKey;

import java.security.PrivateKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LEGACY PAYMENT FLOW
 *
 * This service is retained temporarily for backward compatibility.
 *
 * Offline payment processing is now handled by:
 *
 * OfflinePaymentController
 *      ↓
 * PacketBuilderService
 *      ↓
 * MeshNetworkService
 *      ↓
 * BankServerService
 */

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RSAUtil rsaUtil;

    @Transactional
    @Override
    public PaymentResponse sendMoney(PaymentRequest request) {

        logger.info("Payment request received. Sender: {}, Receiver: {}, Amount: {}",
                request.getSenderUpiId(),
                request.getReceiverUpiId(),
                request.getAmount());

        User sender = userRepository.findByUpiId(request.getSenderUpiId())
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));

        User receiver = userRepository.findByUpiId(request.getReceiverUpiId())
                .orElseThrow(() -> new UserNotFoundException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send money to yourself");
        }

        String paymentData =
                sender.getUpiId()
                        + "|"
                        + receiver.getUpiId()
                        + "|"
                        + request.getAmount();

        PrivateKey privateKey;

        try {

            privateKey = rsaUtil.getPrivateKeyFromString(
                    sender.getPrivateKey()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load private key",
                    e
            );

        }

        String signature;

        try {

            signature = rsaUtil.signData(
                    paymentData,
                    privateKey
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to sign payment",
                    e
            );

        }

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (senderWallet.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance() - request.getAmount());

        receiverWallet.setBalance(receiverWallet.getBalance() + request.getAmount());

        walletRepository.save(senderWallet);

        walletRepository.save(receiverWallet);

        Payment payment = Payment.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .status(PaymentStatus.SUCCESS)
                .signature(signature)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        String verifyData =
                sender.getUpiId()
                        + "|"
                        + receiver.getUpiId()
                        + "|"
                        + savedPayment.getAmount();

        boolean signatureValid;

        try {

            PublicKey publicKey =
                    rsaUtil.getPublicKeyFromString(
                            sender.getPublicKey()
                    );

            signatureValid =
                    rsaUtil.verifySignature(
                            verifyData,
                            savedPayment.getSignature(),
                            publicKey
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to verify signature",
                    e
            );

        }

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .senderUpiId(sender.getUpiId())
                .receiverUpiId(receiver.getUpiId())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus())
                .signatureValid(signatureValid)
                .build();
    }

    @Override
    public List<Payment> getTransactionHistory(String upiId) {

        User user = userRepository.findByUpiId(upiId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Payment> payments = paymentRepository.findBySenderOrReceiver(user, user);

        return payments;
    }
}