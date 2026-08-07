package com.satyam.offline_upi_payment.controller;
import com.satyam.offline_upi_payment.dto.PaymentResponse;

import com.satyam.offline_upi_payment.dto.PaymentRequest;
import com.satyam.offline_upi_payment.entity.Payment;
import com.satyam.offline_upi_payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.satyam.offline_upi_payment.dto.EncryptedPaymentRequest;
import com.satyam.offline_upi_payment.security.AESUtil;
import com.satyam.offline_upi_payment.security.KeyManager;
import com.satyam.offline_upi_payment.security.RSAUtil;
import javax.crypto.SecretKey;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Payment APIs", description = "UPI Payment Operations")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AESUtil aesUtil;
    private final RSAUtil rsaUtil;
    private final KeyManager keyManager;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Send Money",
            description = "Transfers money from one wallet to another."
    )

    @PostMapping("/send")
    public PaymentResponse sendMoney(
            @RequestBody PaymentRequest request,
            org.springframework.security.core.Authentication authentication
    ) {

        request.setSenderUpiId(authentication.getName());

        return paymentService.sendMoney(request);
    }


    @Operation(
            summary = "Transaction History",
            description = "Returns all transactions of a user."
    )

    @GetMapping("/history")
    public List<Payment> getTransactionHistory(
            org.springframework.security.core.Authentication authentication
    ) {

        return paymentService.getTransactionHistory(
                authentication.getName()
        );
    }

    @Operation(
            summary = "Secure Payment",
            description = "Receives AES encrypted payment data and RSA encrypted AES key."
    )
    @PostMapping("/secure-transfer")
    public PaymentResponse secureTransfer(
            @RequestBody EncryptedPaymentRequest request,
            org.springframework.security.core.Authentication authentication
    ) throws Exception {

        String decryptedAESKey = rsaUtil.decrypt(
                request.getEncryptedKey(),
                keyManager.getKeyPair().getPrivate()
        );

        SecretKey secretKey = aesUtil.getSecretKeyFromString(decryptedAESKey);

        String json = aesUtil.decrypt(
                request.getEncryptedData(),
                secretKey,
                request.getIv()
        );

        PaymentRequest paymentRequest =
                objectMapper.readValue(json, PaymentRequest.class);

        paymentRequest.setSenderUpiId(authentication.getName());

        return paymentService.sendMoney(paymentRequest);
    }

    @GetMapping("/public-key")
    public String getPublicKey() {

        return java.util.Base64.getEncoder().encodeToString(
                keyManager.getKeyPair().getPublic().getEncoded()
        );
    }
}