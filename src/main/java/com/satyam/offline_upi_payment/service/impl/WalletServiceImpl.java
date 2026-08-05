package com.satyam.offline_upi_payment.service.impl;

import com.satyam.offline_upi_payment.dto.WalletRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.exception.InsufficientBalanceException;
import com.satyam.offline_upi_payment.exception.InvalidAmountException;
import com.satyam.offline_upi_payment.exception.UserNotFoundException;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;

    private final WalletRepository walletRepository;

    @Override
    public double getBalance(String upiId) {

        User user = userRepository.findByUpiId(upiId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return wallet.getBalance();
    }

    @Override
    public void creditWallet(WalletRequest request) {

        User user = userRepository.findByUpiId(request.getUpiId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);
    }

    @Override
    public void debitWallet(WalletRequest request) {
        User user = userRepository.findByUpiId(request.getUpiId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (wallet.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);
    }
}