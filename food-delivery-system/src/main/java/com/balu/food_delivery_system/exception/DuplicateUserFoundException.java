package com.balu.food_delivery_system.exception;

public class DuplicateUserFoundException extends RuntimeException {
    public DuplicateUserFoundException(String message) {
        super(message);
    }
}
