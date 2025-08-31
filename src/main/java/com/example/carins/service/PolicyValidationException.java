package com.example.carins.service;

public class PolicyValidationException extends RuntimeException {
    public PolicyValidationException(String message) {
        super(message);
    }
}