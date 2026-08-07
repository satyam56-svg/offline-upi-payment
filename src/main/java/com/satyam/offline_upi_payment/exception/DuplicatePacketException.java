package com.satyam.offline_upi_payment.exception;

public class DuplicatePacketException extends RuntimeException {

    public DuplicatePacketException(String message) {
        super(message);
    }

}