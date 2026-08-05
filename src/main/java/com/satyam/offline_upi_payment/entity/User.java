package com.satyam.offline_upi_payment.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String upiId;
    private String publicKey;
    private String password;

    @JsonManagedReference
    @OneToOne(mappedBy = "user")
    private Wallet wallet;
}
