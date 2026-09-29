package com.demaio.jacopo.todoapp.services;

import com.demaio.jacopo.todoapp.dtos.CreateTaskRequest;
import com.demaio.jacopo.todoapp.model.entities.Status;
import com.demaio.jacopo.todoapp.model.entities.Task;
import com.demaio.jacopo.todoapp.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void createTask_whenRequestIsValid_shouldCreateTaskAndSaveIt() {

        LocalDate dueDate = LocalDate.now();

        // Arrange
        CreateTaskRequest createTaskRequest =
                new CreateTaskRequest("Test",
                        "Descrizione",
                        dueDate
                );

        // Act
        Task result = taskService.createTask(createTaskRequest);

        //Assert
        assertEquals("Test", result.getTitle());
        assertEquals("Descrizione", result.getDescription());
        assertEquals(dueDate, result.getDueDate());
        assertEquals(Status.TODO, result.getStatus());

        // Verifichiamo che il service abbia chiamato il metodo save sulla repository una sola volta.
        // any(Task.class) indica che accettiamo qualsiasi oggetto di tipo Task come argomento.
        verify(taskRepository, times(1)).save(any(Task.class));

    }
}
