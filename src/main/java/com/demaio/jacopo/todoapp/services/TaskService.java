package com.demaio.jacopo.todoapp.services;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.dtos.UpdateStatusRequest;
import com.demaio.jacopo.todoapp.dtos.UpdateTaskRequest;
import com.demaio.jacopo.todoapp.exceptions.InvalidTaskDataException;
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
            throw new InvalidTaskDataException("INVALID_TASK_DATA", "Title is required");
        }

        if (taskRequest.dueDate() != null && taskRequest.dueDate().isBefore(LocalDate.now().minusDays(7))) {
            throw new InvalidTaskDataException("INVALID_TASK_DATA", "Due date cannot be more than seven days in the past");
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

    public Task updateTaskDetailsById(Long id, UpdateTaskRequest updateTaskRequest) {
        Task task = findTaskById(id);
        if (updateTaskRequest.title() == null || updateTaskRequest.title().isBlank()) {
            throw new InvalidTaskDataException("INVALID_TASK_DATA", "Title is required");
        }
        task.setTitle(updateTaskRequest.title());

        if (updateTaskRequest.description() != null && !updateTaskRequest.description().isBlank()) {
            if (updateTaskRequest.description().length() > 255) {
                throw new InvalidTaskDataException("INVALID_TASK_DATA", "Description cannot exceed 255 characters");
            }
            task.setDescription(updateTaskRequest.description());
        }


        if (updateTaskRequest.dueDate() != null) {
            if (updateTaskRequest.dueDate().isBefore(LocalDate.now().minusDays(7))) {
                throw new InvalidTaskDataException("INVALID_TASK_DATA", "Due date cannot be more than seven days in the past");
            } else {
                task.setDueDate(updateTaskRequest.dueDate());
            }
        }


        return taskRepository.save(task);
    }

    public Task updateTaskStatus(long id, UpdateStatusRequest updateStatusRequest) {
        Task task = findTaskById(id);

        if (updateStatusRequest.status() == null) {
            throw new InvalidTaskDataException("INVALID_TASK_DATA", "Status is required");
        }

        task.setStatus(updateStatusRequest.status());
        return taskRepository.save(task);
    }

    public void deleteTaskById(long id) {
        Task task = findTaskById(id);
        taskRepository.deleteById(task.getId());
    }
}
