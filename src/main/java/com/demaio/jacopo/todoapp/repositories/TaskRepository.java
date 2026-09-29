package com.demaio.jacopo.todoapp.repositories;

import com.demaio.jacopo.todoapp.model.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
