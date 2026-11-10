package com.demaio.jacopo.todoapp.dtos;

import com.demaio.jacopo.todoapp.model.entities.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull
        Status status
) {
}
