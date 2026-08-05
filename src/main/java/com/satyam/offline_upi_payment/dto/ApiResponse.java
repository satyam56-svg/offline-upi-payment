package com.satyam.offline_upi_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse {

    private boolean success;

    private String message;

    private LocalDateTime timestamp;

}