package com.satyam.offline_upi_payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.satyam.offline_upi_payment.dto.ApiResponse;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUpiIdException.class)
    public ResponseEntity<ApiResponse> handleDuplicateUpiId(DuplicateUpiIdException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ApiResponse> handleInvalidAmount(InvalidAmountException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse> handleInsufficientBalance(InsufficientBalanceException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<ApiResponse> handleInvalidSignature(
            InvalidSignatureException ex
    ) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(PacketExpiredException.class)
    public ResponseEntity<ApiResponse> handlePacketExpired(
            PacketExpiredException ex
    ) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.REQUEST_TIMEOUT
        );
    }

    @ExceptionHandler(DuplicatePacketException.class)
    public ResponseEntity<ApiResponse> handleDuplicatePacket(
            DuplicatePacketException ex
    ) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(SenderMismatchException.class)
    public ResponseEntity<ApiResponse> handleSenderMismatch(
            SenderMismatchException ex
    ) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }
}