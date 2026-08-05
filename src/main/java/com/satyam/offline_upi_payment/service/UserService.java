package com.satyam.offline_upi_payment.service;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;

public interface UserService {
    User registerUser(UserRegistrationRequest request);
}
