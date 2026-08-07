package com.satyam.offline_upi_payment.exception;

public class SenderMismatchException extends RuntimeException {

    public SenderMismatchException(String message) {
        super(message);
    }

}