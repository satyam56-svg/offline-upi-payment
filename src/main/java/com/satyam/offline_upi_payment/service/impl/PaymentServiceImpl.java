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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional
    @Override
    public Payment sendMoney(PaymentRequest request) {

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
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getTransactionHistory(String upiId) {

        User user = userRepository.findByUpiId(upiId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Payment> payments = paymentRepository.findBySenderOrReceiver(user, user);

        return payments;
    }
}