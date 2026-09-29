package com.demaio.jacopo.todoapp.services;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.model.entities.Task;
import com.demaio.jacopo.todoapp.repositories.TaskRepository;

import java.time.LocalDate;

public class TaskService {

    private TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(CreateTaskRequest taskRequest) {
        if (taskRequest.title() == null) {
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
}
