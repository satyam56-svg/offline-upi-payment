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

    @Lob
    @Column(name = "public_key", columnDefinition = "LONGTEXT")
    private String publicKey;
    private String password;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String privateKey;

    @JsonManagedReference
    @OneToOne(mappedBy = "user")
    private Wallet wallet;
}
