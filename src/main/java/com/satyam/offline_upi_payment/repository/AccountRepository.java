package com.satyam.offline_upi_payment.repository;

import com.satyam.offline_upi_payment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

}