package com.satyam.offline_upi_payment.repository;

import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySenderOrReceiver(User sender, User receiver);
}