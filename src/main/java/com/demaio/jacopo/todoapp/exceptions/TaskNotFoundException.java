package com.demaio.jacopo.todoapp.exceptions;


public class TaskNotFoundException extends RuntimeException {

    private final String errorCode;


    public TaskNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
