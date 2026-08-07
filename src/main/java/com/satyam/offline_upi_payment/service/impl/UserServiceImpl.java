package com.satyam.offline_upi_payment.service.impl;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.exception.DuplicateUpiIdException;
import com.satyam.offline_upi_payment.exception.UserNotFoundException;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.satyam.offline_upi_payment.entity.Wallet;
import com.satyam.offline_upi_payment.repository.WalletRepository;
import com.satyam.offline_upi_payment.dto.LoginRequest;
import com.satyam.offline_upi_payment.dto.LoginResponse;
import com.satyam.offline_upi_payment.security.JwtUtil;
import com.satyam.offline_upi_payment.security.RSAUtil;
import java.security.KeyPair;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RSAUtil rsaUtil;


    @Override
    public User registerUser(UserRegistrationRequest request){

        if (userRepository.existsByUpiId(request.getUpiId())) {
            throw new DuplicateUpiIdException("UPI ID already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUpiId(request.getUpiId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        KeyPair keyPair;

        try {
            keyPair = rsaUtil.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }

        String publicKey = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());

        String privateKey = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());

        user.setPublicKey(publicKey);
        user.setPrivateKey(privateKey);

        System.out.println("Public Key Length = " + publicKey.length());
        System.out.println(publicKey);
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

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUpiId());

        return new LoginResponse(
                token,
                user.getUpiId(),
                user.getFullName()
        );
    }
}
