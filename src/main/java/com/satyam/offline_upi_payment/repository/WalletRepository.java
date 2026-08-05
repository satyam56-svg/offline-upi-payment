package com.satyam.offline_upi_payment.repository;

import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
}