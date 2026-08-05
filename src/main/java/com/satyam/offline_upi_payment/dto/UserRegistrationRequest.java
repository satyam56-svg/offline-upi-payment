package com.satyam.offline_upi_payment.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UserRegistrationRequest {

    private String fullName;
    private String upiId;
}
