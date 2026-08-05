package com.satyam.offline_upi_payment.service;

import com.satyam.offline_upi_payment.dto.WalletRequest;

public interface WalletService {

    double getBalance(String upiId);
    void creditWallet(WalletRequest request);
    void debitWallet(WalletRequest request);
}
