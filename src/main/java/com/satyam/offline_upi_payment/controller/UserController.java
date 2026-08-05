package com.satyam.offline_upi_payment.controller;
import com.satyam.offline_upi_payment.dto.LoginRequest;
import com.satyam.offline_upi_payment.dto.LoginResponse;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User APIs", description = "User Registration and Authentication")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register User",
            description = "Registers a new user with a unique UPI ID."
    )
    @PostMapping("/register")
    public User registerUser(@RequestBody UserRegistrationRequest request) {
        return userService.registerUser(request);
    }

    @Operation(
            summary = "User Login",
            description = "Authenticates the user and returns a JWT token."
    )
    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }
}
