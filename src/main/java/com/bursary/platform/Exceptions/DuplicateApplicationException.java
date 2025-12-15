package com.bursary.platform.Exceptions;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(String message) {
        super(message);
    }
}