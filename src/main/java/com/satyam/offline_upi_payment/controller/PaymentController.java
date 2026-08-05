package com.satyam.offline_upi_payment.controller;

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
    public Payment sendMoney(@RequestBody PaymentRequest request) {
        return paymentService.sendMoney(request);
    }


    @Operation(
            summary = "Transaction History",
            description = "Returns all transactions of a user."
    )
    @GetMapping("/history/{upiId}")
    public List<Payment> getTransactionHistory(@PathVariable String upiId) {

        return paymentService.getTransactionHistory(upiId);
    }


    @Operation(
            summary = "Secure Payment",
            description = "Receives AES encrypted payment data and RSA encrypted AES key."
    )
    @PostMapping("/secure-transfer")
    public Payment secureTransfer(
            @RequestBody EncryptedPaymentRequest request) throws Exception {

        String decryptedAESKey = rsaUtil.decrypt(
                request.getEncryptedKey(),
                keyManager.getKeyPair().getPrivate()
        );

        SecretKey secretKey = aesUtil.getSecretKeyFromString(decryptedAESKey);

        String json = aesUtil.decrypt(
                request.getEncryptedData(),
                secretKey
        );

        PaymentRequest paymentRequest =
                objectMapper.readValue(json, PaymentRequest.class);

        Payment payment = paymentService.sendMoney(paymentRequest);

        return payment;
    }
}