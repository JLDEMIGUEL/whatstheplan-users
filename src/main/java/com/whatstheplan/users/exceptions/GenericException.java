package com.whatstheplan.users.exceptions;

public class GenericException extends RuntimeException {
    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }
}
