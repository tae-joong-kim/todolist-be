package com.navercorp.newbie.todolist.controller;

import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoGetResponseDto;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import com.navercorp.newbie.todolist.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todolist")
public class TodoController {
    
    private final TodoService todoService;
    private final ModelMapper modelMapper;

    @PostMapping
    public TodoGetResponseDto createTodo(TodoCreateForm todoCreateForm) throws IOException {
        Todo todo = todoService.createTodo(todoCreateForm);
        return modelMapper.map(todo, TodoGetResponseDto.class);
    }

    @GetMapping
    public List<TodoGetResponseDto> readTodo(){
        return todoService.readTodo().stream()
                .map(todo -> modelMapper.map(todo, TodoGetResponseDto.class))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public TodoGetResponseDto UpdateTodo(@PathVariable Long id, TodoUpdateForm todoUpdateForm) throws IOException {
        Todo todo = todoService.updateTodo(id, todoUpdateForm);
        return modelMapper.map(todo, TodoGetResponseDto.class);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
    }
}
