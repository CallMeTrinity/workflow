package org.example.exception;

public class InvalidDateInputException extends RuntimeException {
    public InvalidDateInputException(String message) {
        super(message);
    }
}
