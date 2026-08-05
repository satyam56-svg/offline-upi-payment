package com.satyam.offline_upi_payment.service.impl;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.repository.UserRepository;
import com.satyam.offline_upi_payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User registerUser(UserRegistrationRequest request){
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUpiId(request.getUpiId());
        return userRepository.save(user);
    }
}
