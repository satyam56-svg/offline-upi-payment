package com.satyam.offline_upi_payment.exception;

public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException(String message) {
        super(message);
    }

}