package com.demaio.jacopo.todoapp.services;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.exceptions.TaskNotFoundException;
import com.demaio.jacopo.todoapp.model.entities.Task;
import com.demaio.jacopo.todoapp.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(CreateTaskRequest taskRequest) {
        if (taskRequest.title() == null || taskRequest.title().isBlank()) {
            throw new IllegalArgumentException();
        }

        if (taskRequest.dueDate() != null && taskRequest.dueDate().isBefore(LocalDate.now().minusDays(7))) {
            throw new IllegalArgumentException();
        }

        Task task = new Task(
                taskRequest.title(),
                taskRequest.description(),
                taskRequest.dueDate()
        );

        taskRepository.save(task);
        return task;
    }

    public Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("TASK_NOT_FOUND", "Task not found with id: " + id));
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }
}
