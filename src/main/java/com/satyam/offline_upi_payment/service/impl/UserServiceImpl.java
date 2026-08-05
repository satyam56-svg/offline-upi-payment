package com.satyam.offline_upi_payment.service.impl;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.exception.DuplicateUpiIdException;
import com.satyam.offline_upi_payment.exception.UserNotFoundException;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.dto.LoginRequest;
import com.satyam.offline_upi_payment.dto.LoginResponse;
import com.satyam.offline_upi_payment.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtUtil jwtUtil;

    @Override
    public User registerUser(UserRegistrationRequest request){

        if (userRepository.existsByUpiId(request.getUpiId())) {
            throw new DuplicateUpiIdException("UPI ID already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUpiId(request.getUpiId());
        user.setPassword(request.getPassword());

        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        wallet.setUser(savedUser);

        walletRepository.save(wallet);
        savedUser.setWallet(wallet);
        return savedUser;
    }

    @Override
    public LoginResponse  loginUser(LoginRequest request) {

        User user = userRepository.findByUpiId(request.getUpiId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUpiId());
        return new LoginResponse(token);
    }
}
