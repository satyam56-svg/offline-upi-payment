package com.satyam.offline_upi_payment.service;
import com.satyam.offline_upi_payment.dto.LoginResponse;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.dto.LoginRequest;

public interface UserService {
    User registerUser(UserRegistrationRequest request);
    LoginResponse loginUser(LoginRequest request);}
