package com.satyam.offline_upi_payment.exception;

public class PacketExpiredException extends RuntimeException {

    public PacketExpiredException(String message) {
        super(message);
    }

}