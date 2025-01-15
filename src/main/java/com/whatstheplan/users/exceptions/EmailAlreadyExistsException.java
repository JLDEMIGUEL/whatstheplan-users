package com.whatstheplan.users.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
