package com.satyam.offline_upi_payment.controller;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody UserRegistrationRequest request) {
        return userService.registerUser(request);
    }
}
