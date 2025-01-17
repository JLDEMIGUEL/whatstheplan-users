package com.whatstheplan.users.exceptions;

public class UserNotExistsException extends RuntimeException {
    public UserNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
