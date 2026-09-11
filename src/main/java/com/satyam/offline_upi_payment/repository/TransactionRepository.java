package com.satyam.offline_upi_payment.repository;

import com.satyam.offline_upi_payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop20ByOrderByIdDesc();

    boolean existsByPacketHash(String packetHash);
}