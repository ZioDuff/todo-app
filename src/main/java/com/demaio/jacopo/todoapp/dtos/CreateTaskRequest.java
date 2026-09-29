package com.demaio.jacopo.todoapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        String description,
        LocalDate dueDate
) {
}
