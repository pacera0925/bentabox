package com.paulcera.bentabox.security.exception;

public class AlreadyLoggedInException extends RuntimeException {

    public AlreadyLoggedInException(String message) {
        super(message);
    }

}
