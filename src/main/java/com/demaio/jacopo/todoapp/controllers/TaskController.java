package com.demaio.jacopo.todoapp.controllers;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.dtos.UpdateStatusRequest;
import com.demaio.jacopo.todoapp.dtos.UpdateTaskRequest;
import com.demaio.jacopo.todoapp.model.entities.Task;
import com.demaio.jacopo.todoapp.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id) {
        Task task = taskService.findTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        return this.taskService.createTask(createTaskRequest);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.findAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{id}/details")
    public ResponseEntity<Task> updateTaskDetails(@PathVariable long id, @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {
        Task task = taskService.updateTaskDetailsById(id, updateTaskRequest);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable long id, @Valid @RequestBody UpdateStatusRequest updateStatusRequest) {
        Task task = taskService.updateTaskStatus(id, updateStatusRequest);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
