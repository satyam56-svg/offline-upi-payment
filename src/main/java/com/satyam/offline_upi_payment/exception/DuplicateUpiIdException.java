package com.satyam.offline_upi_payment.exception;

public class DuplicateUpiIdException extends RuntimeException {

    public DuplicateUpiIdException(String message) {
        super(message);
    }
}