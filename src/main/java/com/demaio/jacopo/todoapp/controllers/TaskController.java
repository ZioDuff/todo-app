package com.demaio.jacopo.todoapp.controllers;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.model.entities.Task;
import com.demaio.jacopo.todoapp.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        return this.taskService.createTask(createTaskRequest);
    }
}
