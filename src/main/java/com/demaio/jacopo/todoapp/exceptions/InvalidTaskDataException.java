package com.demaio.jacopo.todoapp.exceptions;

public class InvalidTaskDataException extends RuntimeException {

    private final String errorCode;

    public InvalidTaskDataException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
