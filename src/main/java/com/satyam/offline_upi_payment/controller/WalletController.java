package com.satyam.offline_upi_payment.controller;

import com.satyam.offline_upi_payment.dto.WalletRequest;
import com.satyam.offline_upi_payment.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Wallet APIs", description = "Wallet Management")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(
            summary = "Check Wallet Balance",
            description = "Returns the current wallet balance of a user."
    )
    @GetMapping("/balance")
    public double getBalance(Authentication authentication) {
        String upiId = authentication.getName();
        return walletService.getBalance(upiId);
    }

    @Operation(
            summary = "Credit Wallet",
            description = "Adds money to the user's wallet."
    )
    @PostMapping("/credit")
    public String creditWallet(@RequestBody WalletRequest request) {

        walletService.creditWallet(request);

        return "Wallet credited successfully";
    }

    @Operation(
            summary = "Debit Wallet",
            description = "Deducts money from the user's wallet."
    )
    @PostMapping("/debit")
    public String debitWallet(@RequestBody WalletRequest request) {

        walletService.debitWallet(request);

        return "Wallet debited successfully";
    }

}
