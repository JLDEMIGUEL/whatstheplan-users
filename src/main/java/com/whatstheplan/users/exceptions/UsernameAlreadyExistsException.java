package com.whatstheplan.users.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
