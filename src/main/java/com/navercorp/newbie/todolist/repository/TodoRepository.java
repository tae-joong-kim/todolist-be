package com.navercorp.newbie.todolist.repository;

import com.navercorp.newbie.todolist.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Optional<Todo> findByStoreFileName(String storeFileName);
}
