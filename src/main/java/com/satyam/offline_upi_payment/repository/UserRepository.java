package com.satyam.offline_upi_payment.repository;
import com.satyam.offline_upi_payment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUpiId(String upiId);
}
