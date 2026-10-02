package com.demaio.jacopo.todoapp.dtos;

public record ErrorResponse(
        String errorCode,
        String message
) {
}
